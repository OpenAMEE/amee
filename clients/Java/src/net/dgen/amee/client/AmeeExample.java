/**
* This file is part of AMEE.
*
* AMEE is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
*
* AMEE is free software and is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* Created by http://www.dgen.net.
* Website http://www.amee.cc
*/
/*
 * AmeeExample.java
 *
 * Created on 04 October 2007, 15:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.dgen.amee.client;

import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.model.profile.AmeeProfile;
import net.dgen.amee.client.model.profile.AmeeProfileCategory;
import net.dgen.amee.client.model.profile.AmeeProfileItem;
import net.dgen.amee.client.service.AmeeContext;
import net.dgen.amee.client.service.AmeeObjectFactory;
import net.dgen.amee.client.util.Choice;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nalu
 */
public abstract class AmeeExample extends JFrame {
    protected AmeeObjectFactory objectFactory = AmeeObjectFactory.getInstance();
    protected AmeeProfile ameeProfile;
    protected JLabel resultLabel = new JLabel("Make selection for fuel and size above. Distance is set at 1000 km.");
    protected String path;
    
    public static void setLoginDetails(){
        //NOTE: Please replace the following with your login details
        AmeeContext.getInstance().setUsername("load");
        AmeeContext.getInstance().setPassword("l04d");
        AmeeContext.getInstance().setBaseUrl("http://local.stage.co2.dgen.net");        
    }
    
    /**
     * Gets or creates the profile and setups up the UI
     */
    public AmeeExample(String profileUID, String path) {
        this.path=path;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("AMEE simple example");
        try { //Try using this profile
            ameeProfile = objectFactory.getProfile(profileUID);
        } catch (AmeeException ex) {
            try {
                //ex.printStackTrace();
                ameeProfile = objectFactory.getProfile();
                System.out.println("CREATING NEW PROFILE");
            } catch (AmeeException ex2) {
                ex2.printStackTrace();
            }
        }
        
        try {//If it doesn't exist, create one - see NOTE above
            System.out.println("Profile UID = "+ameeProfile.getUid());
            addComboBoxes();
        } catch (AmeeException ex) {
            ex.printStackTrace();
        }
        getContentPane().add(resultLabel,java.awt.BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }
    
    protected void createProfileItem(AmeeDataItem ameeDataItem) throws AmeeException {
        System.out.println("duid="+ameeDataItem.getUid());
        AmeeProfileCategory profileCategory = objectFactory.getProfileCategory(ameeProfile, path);
        profileCategory.fetch();//the fetch() method forces a call to the AMEE API
        //Delete all items found in the profileCategory
        //trying to create an identical item of the same (default) name will fail
        for (AmeeProfileItem item : profileCategory.getProfileItems()) {
            item.fetch();
            item.delete();
        }
        //Now set the distance driven per month to 1000 - hardwired for simplicity, but could easily get from user
        List<Choice> values = new ArrayList<Choice>();
        values.add(new Choice("distanceKmPerMonth", "1000"));
        AmeeProfileItem profileItem = profileCategory.addProfileItem(ameeDataItem, values);
        BigDecimal result = profileItem.getAmountPerMonth();
        System.out.println("amountPerMonth="+profileItem.getAmountPerMonth());
        resultLabel.setText("1000 km per month emits "+result+" kgCO2");
    }
    
    public abstract void addComboBoxes() throws AmeeException;
    public abstract void processComboSelection(java.awt.event.ActionEvent evt) throws AmeeException;
}

