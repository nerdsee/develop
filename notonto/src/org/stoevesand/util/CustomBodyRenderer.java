package org.stoevesand.util;
import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.renderkit.BodyRenderer;

public class CustomBodyRenderer extends BodyRenderer{

   //our array with all the attributes of h:body tag
   public static final String[] BODY_ATTRS = {
       "dir",
       "lang",
       "onclick",
       "ondblclick",
       "onkeydown",
       "onkeypress",
       "onkeyup",
       "onmousedown",
       "onmousemove",
       "onmouseout",
       "onmouseover",
       "onmouseup",
       "style",
       "title",
       "onload",
       "onunload"
   };

   @Override
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
       ResponseWriter writer = context.getResponseWriter();
       String clientId = component.getClientId(context);
       writer.startElement("body", component);

       if (shouldWriteId(component)) {
           writer.writeAttribute("id", clientId, "id");
       }

       String styleClass = (String) component.getAttributes().get("styleClass");
       if (styleClass != null && styleClass.length() != 0) {
           writer.writeAttribute("class", styleClass, "styleClass");
       }
       //the only changed line from the original renderer
       renderPassThruAttributes(context, component, BODY_ATTRS);
   }

}