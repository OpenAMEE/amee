package com.amee.domain.sheet;

import com.amee.domain.APIObject;
import com.amee.base.utils.XMLUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Choices implements Serializable, APIObject {

    private String name;
    private List<Choice> choices;

    private Choices() {
        super();
    }

    public Choices(String name, List<Choice> choices) {
        this();
        setName(name);
        setChoices(choices);
    }

    public Choices(String name, String choices) {
        this();
        setName(name);
        setChoices(Choice.parseChoices(choices));
    }

    public static Choices getNewChoices(Choices choices) {
        Choices newChoices = new Choices();
        newChoices.setName(choices.getName());
        List<Choice> c = new ArrayList<Choice>();
        for (Choice choice : choices.getChoices()) {
            c.add(new Choice(choice.getName(), choice.getValue()));
        }
        newChoices.setChoices(c);
        return newChoices;
    }

    public void merge(List<Choice> newChoices) {
        Choice newChoice;
        List<Choice> existingChoices = getChoices();
        // replace existing choices
        for (int i = 0; i < existingChoices.size(); i++) {
            Iterator<Choice> newChoiceIter = newChoices.iterator();
            while (newChoiceIter.hasNext()) {
                newChoice = newChoiceIter.next();
                if (newChoice.equals(existingChoices.get(i))) {
                    // new choice matches and replaces an existing choice
                    existingChoices.set(i, newChoice);
                    newChoiceIter.remove();
                    break;
                }
            }
        }
        // add remaining new choices
        existingChoices.addAll(newChoices);
    }

    public boolean containsKey(String key) {
        return getChoices().contains(new Choice(key));
    }

    public <T> T getKeyFor(String value, Class<T> type){
        T ret = null;
        String val = null;
        for(Choice c : choices){
            if(value.equalsIgnoreCase(c.getValue())){
                val = c.getName();
                break;
            }
        }
        if(type == String.class){
            ret = type.cast(val);
        }else if(type == BigDecimal.class){
            try{
                BigDecimal bd = new BigDecimal(val);
                ret = type.cast(bd);
            }catch(NumberFormatException nfe){
                //swallow
            }
        }else{
            try{
                ret = type.cast(val);
            }catch(ClassCastException cce){
                //swallow
            }
        }
        return ret;
    }

    public Choice get(String key) {
        int index = getChoices().indexOf(new Choice(key));
        if (index >= 0) {
            return getChoices().get(index);
        } else {
            return null;
        }
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", getName());
        JSONArray choicesJSONArray = new JSONArray();
        for (Choice choice : getChoices()) {
            choicesJSONArray.put(choice.getJSONObject());
        }
        obj.put("choices", choicesJSONArray);
        return obj;
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        return getJSONObject();
    }


    public JSONObject getIdentityJSONObject() throws JSONException {
        return new JSONObject();
    }

    public Element getElement(Document document) {
        Element element = document.createElement("Choices");
        element.appendChild(XMLUtils.getElement(document, "Name", getName()));
        Element choicesElement = document.createElement("Choices");
        for (Choice choice : getChoices()) {
            choicesElement.appendChild(choice.getElement(document));
        }
        element.appendChild(choicesElement);
        return element;
    }

    public Element getElement(Document document, boolean detailed) {
        return getElement(document);
    }

    public Element getIdentityElement(Document document) {
        return document.createElement("Choices");
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    protected void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}