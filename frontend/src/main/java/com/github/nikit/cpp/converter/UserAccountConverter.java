package com.github.nikit.cpp.converter;

import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.UserAccount;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.stream.Collectors;

public class UserAccountConverter {
    public static UserAccountDetailsDTO convertToUserAccountDetailsDTO(UserAccount userAccount) {
        if (userAccount == null) { return null; }
        return new UserAccountDetailsDTO(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getAvatar(),
                userAccount.getPassword(),
                userAccount.isExpired(),
                userAccount.isLocked(),
                userAccount.isEnabled(),
                convertRoles(userAccount.getRoles())
        );
    }

    private static Collection<SimpleGrantedAuthority> convertRoles(Collection<String> roles) {
        if (roles==null) {return null;}
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

}
