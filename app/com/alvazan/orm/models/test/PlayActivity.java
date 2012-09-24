package com.alvazan.orm.models.test;

import org.joda.time.LocalDateTime;

import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.api.base.anno.NoSqlId;
import com.alvazan.orm.api.base.anno.NoSqlIndexed;
import com.alvazan.orm.api.base.anno.NoSqlManyToOne;

@NoSqlEntity
public class PlayActivity {

    @NoSqlId
    private String id;
    
    @NoSqlManyToOne
    @NoSqlIndexed
    private PlayAccount account;
    
    @NoSqlIndexed
    private String uniqueColumn;

    @NoSqlIndexed
    private String name;
    
    @NoSqlIndexed
    private LocalDateTime date;
    
    @NoSqlIndexed
    private long numTimes;

    @NoSqlIndexed
    private Boolean isCool;
    @NoSqlIndexed 
    private float myFloat;
    
    private String somethingElse;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PlayAccount getAccount() {
        return account;
    }

    public void setAccount(PlayAccount account) {
        this.account = account;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public String getUniqueColumn() {
        return uniqueColumn;
    }

    public void setUniqueColumn(String uniqueColumn) {
        this.uniqueColumn = uniqueColumn;
    }

    public long getNumTimes() {
        return numTimes;
    }

    public void setNumTimes(long numTimes) {
        this.numTimes = numTimes;
    }

    public float getMyFloat() {
        return myFloat;
    }

    public void setMyFloat(float myFloat) {
        this.myFloat = myFloat;
    }

    public String getSomethingElse() {
        return somethingElse;
    }

    public void setSomethingElse(String somethingElse) {
        this.somethingElse = somethingElse;
    }

    
    public Boolean getIsCool() {
        return isCool;
    }

    public void setIsCool(Boolean isCool) {
        this.isCool = isCool;
    }
}
