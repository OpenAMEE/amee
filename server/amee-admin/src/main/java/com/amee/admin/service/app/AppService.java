package com.amee.admin.service.app;

import com.amee.domain.Pager;
import com.amee.domain.auth.Action;
import com.amee.domain.site.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Service
public class AppService {

    @Autowired
    private AppServiceDAO dao;


    public App getAppByUid(String uid) {
        return dao.getAppByUid(uid);
    }

    public App getAppByName(String name) {
        return dao.getAppByName(name);
    }

    public List<App> getApps(Pager pager) {
        return dao.getApps(pager);
    }

    public List<App> getApps() {
        return dao.getApps();
    }

    public void save(App app) {
        dao.save(app);
    }

    public void remove(App app) {
        dao.remove(app);
    }

    public Action getActionByUid(App app, String uid) {
        return dao.getActionByUid(app, uid);
    }

    public Action getActionByUid(String uid) {
        return dao.getActionByUid(uid);
    }

    public Action getActionByKey(String key) {
        return dao.getActionByKey(key);
    }

    public List<Action> getActions(App app) {
        return dao.getActions(app);
    }

    public List<Action> getActions(App app, Pager pager) {
        return dao.getActions(app, pager);
    }

    public List<Action> getActions(Pager pager) {
        return dao.getActions(pager);
    }

    public void save(Action action) {
        dao.save(action);
    }

    public void remove(Action action) {
        dao.remove(action);
    }   
}
