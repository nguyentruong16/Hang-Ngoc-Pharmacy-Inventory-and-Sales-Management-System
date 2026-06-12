CREATE TABLE passwordresettoken (
    tokenID INT NOT NULL AUTO_INCREMENT,
    accountID INT NOT NULL,
    tokenHash VARCHAR(64) NOT NULL,
    expiresAt DATETIME NOT NULL,
    usedAt DATETIME NULL,
    createdAt DATETIME NOT NULL,
    PRIMARY KEY (tokenID),
    UNIQUE KEY uk_passwordresettoken_tokenHash (tokenHash),
    KEY idx_passwordresettoken_accountID (accountID),
    CONSTRAINT fk_passwordresettoken_account
        FOREIGN KEY (accountID) REFERENCES account (accountID)
);
