package com.github.nkonev.mongo.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.util.DBObjectUtils;

import java.util.*;

@ChangeLog
public class TestChangelog {
    @ChangeSet(order = "001", id = "1", author = "nkonev")
    public void importantWorkToDo(DB db){
        // task implementation
        db.dropDatabase();

        List<DBObject> dbObjects =  new ArrayList<>();
        for (int i =0; i< 1000; ++i) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", "test_chat_"+i);
            map.put("participants", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
            DBObject dbObject = BasicDBObjectBuilder.start(map).get();
            dbObjects.add(dbObject);
        }
        db.getCollection("metainfo").insert(dbObjects);
    }
}
