package com.vaadin.componentfactory.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.RouterLink;

@SuppressWarnings("serial")
public class MainLayout extends AppLayout {

  public MainLayout() {
    final DrawerToggle drawerToggle = new DrawerToggle();

    final RouterLink basicUseDemo = new RouterLink("Basic use", InputMaskDemoView.class);
    final RouterLink binderWithMaskedValueDemo =
        new RouterLink("Binder use", BinderDemoView.class);
    final RouterLink binderWithUnmaskedValueDemo =
        new RouterLink("Binding unmasked value", BinderWithUnmaskedValueDemoView.class);

    final VerticalLayout menuLayout =
        new VerticalLayout(basicUseDemo, binderWithMaskedValueDemo, binderWithUnmaskedValueDemo);

    addToDrawer(menuLayout);
    addToNavbar(drawerToggle);
  }
  
  /**
   * Additional code used in the demo
   */
  protected static final String PHONE_MASK = "(000) 000-0000";
  protected static final String DATE_MASK = "00/00/0000";
  protected static final String DATE_FORMAT = "MM/dd/yyyy";

  protected static void addCloseHandler(Component textField, Editor<Person> editor) {
      textField.getElement().addEventListener("keydown", e -> editor.cancel()).setFilter("event.code === 'Escape'");
  }

  protected ValidationResult validatePhone(String phone, ValueContext ctx) {
      if (phone != null && phone.length() < 14) {
          return ValidationResult.error("Enter a valid phone number. Length cannot be less than 14");
      }
      return ValidationResult.ok();
  }

  protected Div createMessageDiv(String id) {
      Div message = new Div();
      message.setId(id);
      message.getStyle().set("whiteSpace", "pre");
      return message;
  }
  
  protected Div createCard(String title, Component... components) {     
      Div card = new Div();
      card.setWidthFull();
      card.addClassName("component-card");
      card.add(new H3(title));
      card.add(components);
      return card;
  }
}
