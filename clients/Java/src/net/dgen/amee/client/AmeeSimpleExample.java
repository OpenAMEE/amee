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
 * AmeeDrillExample.java
 *
 * Created on 02 October 2007, 15:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.dgen.amee.client;

import net.dgen.amee.client.model.base.AmeeCategory;
import net.dgen.amee.client.model.base.AmeeItem;
import net.dgen.amee.client.model.base.AmeeObjectReference;
import net.dgen.amee.client.model.base.AmeeObjectType;
import net.dgen.amee.client.model.base.AmeeValue;
import net.dgen.amee.client.model.data.AmeeDataCategory;
import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.model.data.AmeeDrillDown;
import net.dgen.amee.client.model.profile.AmeeProfile;
import net.dgen.amee.client.model.profile.AmeeProfileCategory;
import net.dgen.amee.client.model.profile.AmeeProfileItem;
import net.dgen.amee.client.service.AmeeContext;
import net.dgen.amee.client.service.AmeeObjectFactory;
import net.dgen.amee.client.util.Choice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.awt.event.*;
import java.math.BigDecimal;
import javax.swing.*;

/** This opens a window and allows the user to choose the fuel
 *  and size of car - km per month is hard-wired to 1000.
 *  The possible choices of fuel and size are hardcoded.
 *  See AmeeDrillExample for how to use the drill down to
 *  dynamically discover choices.
 */
public class AmeeSimpleExample extends AmeeExample {
    private JComboBox fuelBox;
    private JComboBox sizeBox;
    
    public AmeeSimpleExample(String profileUID, String path){
        super(profileUID,path);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //NOTE: Please enter your login details in AmeeExample.setLoginDetails()
        AmeeExample.setLoginDetails();
        
        //NOTE: Please replace profileUid with one that you know exists, otherwise
        //a new profile will be created everytime. Look in the stdout and
        //it'll give you the uid of a newly created profile
        //String profileUID="2B9DFBFE86FE";
        String profileUID="74050B631066";
        AmeeSimpleExample ameeExample = new AmeeSimpleExample(profileUID,"transport/car/generic");
        long end = Calendar.getInstance().getTimeInMillis();
    }
    
    /** Creates combo boxes by drilling down.
     *  Note: the choices for subsequent combo boxes will be for the first choice
     *  in the preceding combo box. For example, if first combo box is Diesel|Petrol|Petrol Hybrid
     *  then the second combo box will be populated with options for Diesel.
     *  This might not work for other profile categories.
     *  @param jf The JFrame to which the combo boxes will be added.
     */
    public void addComboBoxes() throws AmeeException{
        //Combo boxes will be added to this panel
        JPanel jp = new JPanel();
        getContentPane().add(jp);
        
        String[] fuelChoices = {"fuel","petrol","diesel"};
        fuelBox = new JComboBox(fuelChoices);
        String[] sizeChoices = {"size","small","medium","large"};
        sizeBox = new JComboBox(sizeChoices);
        
        jp.add(fuelBox);
        jp.add(sizeBox);
        
        ActionListener al = new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                try {
                    processComboSelection(evt);
                } catch (AmeeException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
        fuelBox.addActionListener(al);
        sizeBox.addActionListener(al);
    }
    
    /** This method processes the selection in one of the combo boxes */
    public void processComboSelection(ActionEvent evt) throws AmeeException {
        //First get the name and value of the choice selected by the user
        if(fuelBox.getSelectedIndex()==0 || sizeBox.getSelectedIndex()==0){
            System.out.println("Further selections to be made.");
            return; //user still has to make selection(s)
        }
        
        String fuel=fuelBox.getSelectedItem().toString();
        String size=sizeBox.getSelectedItem().toString();
        
        System.out.println("fuel="+fuel+", size="+size);
        
        AmeeDrillDown ameeDrillDown = objectFactory.getDrillDown(path+"/drill");
        //Now add the selection, first removing any existing selections of that name
        ameeDrillDown.addSelection("fuel",fuel);
        ameeDrillDown.addSelection("size",size);
        ameeDrillDown.fetch(); //the fetch() method forces a call to the AMEE API
        AmeeDataItem ameeDataItem = ameeDrillDown.getDataItem();
        if(ameeDataItem!=null) {
            createProfileItem(ameeDataItem);
        } else //shouldn't be null unless incorrect parameter values were offered in combo boxes
            System.out.println("duid is null - a selected combo box items is incorrect");
    }
    
    
    
}
