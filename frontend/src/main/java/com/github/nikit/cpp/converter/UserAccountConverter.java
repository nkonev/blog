package com.github.nikit.cpp.converter;

import com.github.nikit.cpp.ApiConstants;
import com.github.nikit.cpp.dto.EditUserDTO;
import com.github.nikit.cpp.dto.UserAccountDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.entity.jpa.UserRole;
import com.github.nikit.cpp.exception.BadRequestException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

    private static Collection<SimpleGrantedAuthority> convertRoles(Collection<UserRole> roles) {
        if (roles==null) {return null;}
        return roles.stream().map(ur -> new SimpleGrantedAuthority(ur.name())).collect(Collectors.toSet());
    }

    public static UserAccountDTO convertToUserAccountDTO(UserAccount userAccount) {
        if (userAccount == null) { return null; }
        return new UserAccountDTO(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getAvatar()
        );
    }

    public static UserAccountDTO convertToUserAccountDTO(UserAccountDetailsDTO userAccount) {
        if (userAccount == null) { return null; }
        return new UserAccountDTO(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getAvatar()
        );
    }


    private static void validateUserPassword(String password) {
        Assert.notNull(password, "password must be set");
        if (password.length() < ApiConstants.MIN_PASSWORD_LENGTH || password.length() > ApiConstants.MAX_PASSWORD_LENGTH) {
            throw new BadRequestException("password don't match requirements");
        }
    }

    public static UserAccount buildUserAccountEntityForInsert(EditUserDTO userAccountDTO, PasswordEncoder passwordEncoder) {
        final boolean expired = false;
        final boolean locked = false;
        final boolean enabled = false;

        Set<UserRole> newUserRoles = new HashSet<>();
        newUserRoles.add(UserRole.ROLE_USER);

        String password = userAccountDTO.getPassword();
        try {
            validateUserPassword(password);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        return new UserAccount(
                userAccountDTO.getLogin(),
                passwordEncoder.encode(password),
                userAccountDTO.getAvatar(),
                expired,
                locked,
                enabled,
                newUserRoles,
                userAccountDTO.getEmail()
        );
    }

    public static void updateUserAccountEntity(EditUserDTO userAccountDTO, UserAccount userAccount, PasswordEncoder passwordEncoder) {

        String password = userAccountDTO.getPassword();
        if (password!=null) {
            validateUserPassword(password);
            userAccount.setPassword(passwordEncoder.encode(password));
        }
        userAccount.setUsername(userAccountDTO.getLogin());
        userAccount.setAvatar(userAccountDTO.getAvatar());
        userAccount.setEmail(userAccountDTO.getEmail());
    }

    public static EditUserDTO convertToEditUserDto(UserAccount userAccount) {
        EditUserDTO e = new EditUserDTO();
        e.setAvatar(userAccount.getAvatar());
        e.setEmail(userAccount.getEmail());
        e.setLogin(userAccount.getUsername());
        return e;
    }

}
