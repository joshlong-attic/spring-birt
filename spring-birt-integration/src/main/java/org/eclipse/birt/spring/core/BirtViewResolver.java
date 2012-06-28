package org.eclipse.birt.spring.core;

import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

/**
 * Simple {@link org.springframework.web.servlet.ViewResolver} that translates the URL of the report into a report name
 * and then resolves it using the appropriate {@link BirtView Birt view implementation}.
 *
 * @author Josh Long
 */
public class BirtViewResolver extends UrlBasedViewResolver {


    @Override
    protected Class requiredViewClass() {
        return AbstractBirtView.class;
    }


}
