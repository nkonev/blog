package com.github.nkonev.converter;

import com.github.nkonev.ApiConstants;
import com.github.nkonev.dto.*;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.entity.jpa.UserRole;
import com.github.nkonev.exception.BadRequestException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
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
                Collections.singletonList(convertRole(userAccount.getRole())),
                userAccount.getEmail()
        );
    }

    public static UserSelfProfileDTO getUserSelfProfile(UserAccountDetailsDTO userAccount) {
        if (userAccount == null) { return null; }
        return new UserSelfProfileDTO (
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getAvatar(),
                userAccount.getEmail()
        );
    }

    private static SimpleGrantedAuthority convertRole(UserRole role) {
        if (role==null) {return null;}
        return new SimpleGrantedAuthority(role.name());
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

    public static UserAccountDTOExtended convertToUserAccountDTOExtended(UserAccount userAccount) {
        if (userAccount == null) { return null; }
        return new UserAccountDTOExtended(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getAvatar(),
                userAccount.isEnabled(),
                userAccount.isExpired(),
                userAccount.isLocked()
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

        final UserRole newUserRole = UserRole.ROLE_USER;

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
                newUserRole,
                userAccountDTO.getEmail()
        );
    }

    public static void updateUserAccountEntity(EditUserDTO userAccountDTO, UserAccount userAccount, PasswordEncoder passwordEncoder) {

        String password = userAccountDTO.getPassword();
        if (!StringUtils.isEmpty(password)) {
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
