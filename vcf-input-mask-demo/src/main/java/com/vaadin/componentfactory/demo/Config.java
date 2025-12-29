package com.vaadin.componentfactory.demo;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;

@StyleSheet("demo.css")
@StyleSheet(Lumo.STYLESHEET)
public class Config implements AppShellConfigurator {
    
}
