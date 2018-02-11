package com.github.nkonev.blog.dto;

public class UserAccountDTOExtended extends UserAccountDTO {
    private static final long serialVersionUID = 6613889927056965939L;

    private DataDTO managementData;

    public UserAccountDTOExtended() { }

    public UserAccountDTOExtended(Long id, String login, String avatar, boolean enabled, boolean expired, boolean locked) {
        super(id, login, avatar);
        this.managementData = new DataDTO(enabled, expired, locked);
    }

    public DataDTO getManagementData() {
        return managementData;
    }

    public void setManagementData(DataDTO managementData) {
        this.managementData = managementData;
    }


    public static class DataDTO {
        private boolean enabled;
        private boolean expired;
        private boolean locked;

        public DataDTO(boolean enabled, boolean expired, boolean locked) {
            this.enabled = enabled;
            this.expired = expired;
            this.locked = locked;
        }

        public DataDTO() { }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isExpired() {
            return expired;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }
    }
}
