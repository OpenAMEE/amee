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

import net.dgen.amee.client.model.data.AmeeDataItem;
import net.dgen.amee.client.model.data.AmeeDrillDown;
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
 *  The combo boxes are populated with choices using drill down.
 */
public class AmeeDrillExample extends AmeeExample {    
    private AmeeDrillDown ameeDrillDown;
    
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
        AmeeDrillExample ameeExample = new AmeeDrillExample(profileUID,"transport/car/generic");
        long end = Calendar.getInstance().getTimeInMillis();
    }
    
    /**
     * Creates a new instance of AmeeDrillExample
     */
    public AmeeDrillExample(String profileUID, String path) {
        super(profileUID,path);
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
        //First perform a drill down to find available choices for the category specifed by path
        ameeDrillDown = objectFactory.getDrillDown(path+"/drill");
        while (ameeDrillDown.hasChoices()) { //If there are choices...
            JComboBox jcb = new JComboBox(); //... create a combo box to store them
            jp.add(jcb);
            jcb.addItem(ameeDrillDown.getChoiceName()); //first item is the choice name, e.g. fuel
            //System.out.println("choice = "+ameeDrillDown.getChoiceName());
            List list = ameeDrillDown.getChoices();
            for (Object elem : list) { //Now add all choice, for fuel they are: diesel, petrol and petrol hybrid
                //System.out.println("value = "+elem);
                jcb.addItem(elem.toString());
            }
            //In order to get the next level of choices for the drill down, we select the first choice in the list
            ameeDrillDown.addSelection(ameeDrillDown.getChoiceName(), ameeDrillDown.getChoices().get(0).getValue());
            ameeDrillDown.fetch(); //the fetch() method forces a call to the AMEE API
            jcb.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    try {
                        processComboSelection(evt);
                    } catch (AmeeException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        //clear the drill down object so it can now be used for user selection
        ameeDrillDown.clearSelections();
        ameeDrillDown.clearChoices();
    }
    
    /** This method processes the selection in one of the combo boxes */
    public void processComboSelection(ActionEvent evt) throws AmeeException {
        //First get the name and value of the choice selected by the user
        JComboBox jcb = (JComboBox) evt.getSource();
        String name = jcb.getItemAt(0).toString();
        String value = jcb.getSelectedItem().toString();
        System.out.println("name="+name+", value="+value);
        
        //Now add the selection, first removing any existing selections of that name
        List<Choice> selections=ameeDrillDown.getSelections();
        Choice toReplace=null;
        for (Choice sel : selections) {
            if(sel.getName().equals(name)){
                toReplace=sel;
            }
        }
        if(toReplace!=null){
            selections.remove(toReplace);
            ameeDrillDown.setSelections(selections);
        }
        ameeDrillDown.addSelection(name,value);
        ameeDrillDown.fetch(); //the fetch() method forces a call to the AMEE API
        AmeeDataItem ameeDataItem = ameeDrillDown.getDataItem();
        if(ameeDataItem!=null) {//if non null, the drill down is complete - user has made all selections, no choices left
            createProfileItem(ameeDataItem);
            
        } else //if null, there are still choices for the user to select
            System.out.println("datauid is null - user still needs to make selections");
    }
    
    
}
