package com.alvazan.orm.models.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alvazan.orm.api.base.CursorToMany;
import com.alvazan.orm.api.base.CursorToManyImpl;
import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.api.base.anno.NoSqlId;
import com.alvazan.orm.api.base.anno.NoSqlIndexed;
import com.alvazan.orm.api.base.anno.NoSqlManyToMany;
import com.alvazan.orm.api.base.anno.NoSqlOneToMany;


@NoSqlEntity
public class PlayAccountMTM extends PlayAccountSuper{
    @NoSqlId
    private String id;
    
    @NoSqlIndexed
    private String name;
    
    @NoSqlIndexed
    private Float users;

    //@Transient
  	@NoSqlOneToMany(entityType=PlayActivityMTM.class)
    private List<PlayActivityMTM> activities = new ArrayList<PlayActivityMTM>();
    
	@NoSqlManyToMany(entityType=PlayActivityMTM.class)
	private CursorToMany<PlayActivityMTM> activitiesCursor = new CursorToManyImpl<PlayActivityMTM>();

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
    
/*    public void putActivity(PlayActivityMTM act) {
        activities.add(act);
    }

    public PlayActivityMTM getActivity(int index) {
        return activities.get(index);
    }
    
    public void setActivities(List<PlayActivityMTM> act) {
        this.activities =act;
    }*/
    public List<PlayActivityMTM> getActivities() {
        return activities;
    }
    
	public CursorToMany<PlayActivityMTM> getActivitiesCursor() {
		return activitiesCursor;
	}

	public void addActivity(PlayActivityMTM act1) {
		activities.add(act1);
		activitiesCursor.addElement(act1);
	}
    
}
