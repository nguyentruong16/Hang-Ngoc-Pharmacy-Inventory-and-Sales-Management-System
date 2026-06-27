package com.example.project;

import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.entity.Branch;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.repository.BranchRepository;
import com.example.project.security.AccountPrincipal;
import com.example.project.service.CustomAccountDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit test (no Spring, no DB) for how {@link CustomAccountDetailsService} turns AccountPermission
 * rows into the authenticated principal: primary role/branch selection and the fail-closed rule.
 */
@ExtendWith(MockitoExtension.class)
class CustomAccountDetailsServiceTest {

    @Mock
    AccountRepository accountRepository;
    @Mock
    AccountpermissionRepository accountpermissionRepository;
    @Mock
    BranchRepository branchRepository;
    @InjectMocks
    CustomAccountDetailsService service;

    @Test
    void selectedBranchRoleIsTheOnlyGrantedAuthority() {
        when(accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("u", "u"))
                .thenReturn(Optional.of(activeAccount()));
        when(accountpermissionRepository.findProfilePermissionsByAccountId(1))
                .thenReturn(List.of(perm(5, "PHARMACIST", 2)));
        when(branchRepository.findById(2)).thenReturn(Optional.of(branch(2)));
        when(accountpermissionRepository.findByAccountIdAndBranchId(1, 2))
                .thenReturn(List.of(perm(5, "PHARMACIST", 2)));

        AccountPrincipal principal = service.loadUserByUsernameAndBranch("u", 2);

        assertEquals("PHARMACIST", principal.getPrimaryRole());
        assertEquals(Integer.valueOf(2), principal.getBranchId());
        Set<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_PHARMACIST"), authorities);
    }

    @Test
    void accountWithNoValidRoleCannotSignIn() {
        when(accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("u", "u"))
                .thenReturn(Optional.of(activeAccount()));
        when(accountpermissionRepository.findProfilePermissionsByAccountId(1))
                .thenReturn(List.of(perm(1, "SOMETHING_UNKNOWN", 1)));

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsernameAndBranch("u", 1));
    }

    @Test
    void inactiveAccountIsRejected() {
        Account inactive = activeAccount();
        inactive.setStatus(false);
        when(accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("u", "u"))
                .thenReturn(Optional.of(inactive));

        assertThrows(DisabledException.class, () -> service.loadUserByUsernameAndBranch("u", 1));
    }

    private Account activeAccount() {
        Account account = new Account();
        account.setId(1);
        account.setName("Test User");
        account.setUsername("u");
        account.setEmail("u@example.com");
        account.setPassword("$2a$10$examplehashexamplehashexampleha");
        account.setStatus(true);
        return account;
    }

    private Branch branch(Integer id) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setName("Branch " + id);
        return branch;
    }

    private Accountpermission perm(int id, String role, Integer branchId) {
        Accountpermission permission = new Accountpermission();
        permission.setId(id);
        permission.setRole(role);
        if (branchId != null) {
            Branch branch = new Branch();
            branch.setId(branchId);
            permission.setBranchID(branch);
        }
        return permission;
    }
}
