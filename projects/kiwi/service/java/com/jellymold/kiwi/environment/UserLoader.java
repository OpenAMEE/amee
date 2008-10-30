package com.jellymold.kiwi.environment;

import com.csvreader.CsvReader;
import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.GroupUser;
import com.jellymold.kiwi.Role;
import com.jellymold.kiwi.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Service
public class UserLoader implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private SiteService siteService;

    public boolean loadUsers(FileItem fileItem, Environment environment, User cloneUser) {
        log.debug("cloneUser: " + ((cloneUser == null) ? "none" : cloneUser.getUsername()));
        User user;
        GroupUser newGroupUser;
        String username;
        boolean success = false;
        try {
            Charset charset = Charset.forName("ISO-8859-1");
            CsvReader reader = new CsvReader(fileItem.getInputStream(), charset);
            // Read first row as columns
            reader.readHeaders();
            if (reader.getHeaders() != null) {
                // iterate over CSV
                while (reader.readRecord()) {
                    username = reader.get("username");
                    if ((username != null) && (siteService.getUserByUsername(environment, username) == null)) {
                        log.debug("newUser: " + username);
                        user = new User(environment);
                        user.setUsername(username);
                        user.setUid(reader.get("uid"));
                        user.setName(reader.get("name"));
                        user.setPassword(reader.get("password"));
                        user.setEmail(reader.get("email"));
                        siteService.save(user);
                        if (cloneUser != null) {
                            for (GroupUser groupUser : siteService.getGroupUsers(cloneUser)) {
                                newGroupUser = new GroupUser(groupUser.getGroup(), user);
                                for (Role role : groupUser.getRoles()) {
                                    newGroupUser.addRole(role);
                                }
                                siteService.save(newGroupUser);
                            }
                        }
                    }
                }
            }
            reader.close();
            success = true;
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return success;
    }
}
