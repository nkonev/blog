package com.github.nkonev.blog.entity.jpa;

import com.github.nkonev.blog.Constants;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Insert data only from migration
 */
@Entity
@Table(name = "runtime_settings", schema = Constants.Schemas.SETTINGS)
@DynamicInsert
@DynamicUpdate
public class RuntimeSettings {
    @Id
    private String key;

    private String value;

    public RuntimeSettings() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
