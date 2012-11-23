package com.alvazan.orm.models.test;

import java.util.ArrayList;
import java.util.List;

import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.api.base.anno.NoSqlId;
import com.alvazan.orm.api.base.anno.NoSqlIndexed;
import com.alvazan.orm.api.base.anno.NoSqlOneToMany;

@NoSqlEntity
public class PlayAccount extends PlayAccountSuper{
    @NoSqlId
    private String id;
    
    @NoSqlIndexed
    private String name;
    
    @NoSqlIndexed
    private Float users;

    //@Transient
    @NoSqlOneToMany
    private List<PlayActivity> activities = new ArrayList<PlayActivity>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getUsers() {
        return users;
    }

    public void setUsers(Float users) {
        this.users = users;
    }
    
}
