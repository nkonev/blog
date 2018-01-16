package com.github.nkonev.mongo.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.github.nkonev.entity.mongodb.ChatInfo;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import java.util.*;
import static com.github.nkonev.CommonTestConstants.CHAT_USER_ID;

@ChangeLog
public class TestChangelog {

    @ChangeSet(order = "001", id = "1", author = "nkonev")
    public void importantWorkToDo(DB db){
        // task implementation
        db.dropDatabase();

        List<DBObject> dbObjects =  new ArrayList<>();
        for (int i=0; i<1000; ++i) {
            DBObject dbObject = buildDbObject(i, Arrays.asList(CHAT_USER_ID, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
            dbObjects.add(dbObject);
        }
        for (int i=0; i<10; ++i) {
            DBObject dbObject = buildDbObject(i, Arrays.asList(10L, 11L, 12L));
            dbObjects.add(dbObject);
        }
        db.getCollection(ChatInfo.COLLECTION_NAME).insert(dbObjects);
    }

    private DBObject buildDbObject(int suffix, List<Long> participants) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", "test_chat_"+suffix);
        map.put("participants", participants);
        return BasicDBObjectBuilder.start(map).get();
    }
}
