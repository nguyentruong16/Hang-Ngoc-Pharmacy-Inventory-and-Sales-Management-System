package com.example.project.dto.response;

import com.example.project.entity.Producer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProducerResponse {
    private Integer id;
    private String name;

    public static ProducerResponse from(Producer producer) {
        return new ProducerResponse(
                producer.getId(),
                producer.getName()
        );
    }
}