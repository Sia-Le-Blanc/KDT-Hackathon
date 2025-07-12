package com.metamate.domain.login.dto;


import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class LoginSelectDTO
{
    private String userEmail;
    private String Token;
}
