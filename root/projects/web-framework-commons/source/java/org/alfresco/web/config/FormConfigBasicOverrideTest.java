/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FormConfigBasicOverrideTest extends FormConfigBasicTest
{
    // This class inherits all of its test messages from the superclass and simply
    // overrides a number of changed properties - mimicking the changes in the xml.
    @Override
    protected List<String> getConfigFiles()
    {
        List<String> result = new ArrayList<String>(1);
        result.add("test-config-forms-basic.xml");
        result.add("test-config-forms-basic-override.xml");
        return result;
    }

    @Override
    protected String getExpectedMessageForNumericConstraint()
    {
        return "Test Message override";
    }

    @Override
    protected List<ControlParam> getExpectedControlParamsForDText()
    {
        return Arrays.asList(new ControlParam("size", "999"));
    }
    
    @Override
    protected List<ControlParam> getExpectedControlParamsForDTest()
    {
        return Arrays.asList(new ControlParam("a", "Goodbye"),
                new ControlParam("b", null),
                new ControlParam("c", "This is new"));
    }

    @Override
    protected List<String> getExpectedTemplatesForNoAppearanceDefaultForm()
    {
        return Arrays.asList(new String[]{"/view/template/override",
                "/edit/template/override", "/create/template/override"});
    }
    
    @Override
    public void testGetForcedFields()
    {
        List<String> forcedFields = noAppearanceDefaultForm.getForcedFields();
        assertEquals("Wrong forced fields count", 2, forcedFields.size());

        assertTrue("Expected cm:name to be forced", noAppearanceDefaultForm
                .isFieldForced("cm:name"));
        assertTrue("Expected cm:description to be forced", noAppearanceDefaultForm
                .isFieldForced("cm:description"));
        assertFalse("Expected cm:title not to be forced", noAppearanceDefaultForm
                .isFieldForced("cm:title"));
    }

    /**
     * This test checks that the expected JS and CSS resources are available for a
     * default-control.
     */
    @Override
    public void testGetDependenciesForDefaultControl() throws Exception
    {
        DefaultControlsConfigElement defaultControls
                = (DefaultControlsConfigElement)globalDefaultControls;
        
        Map<String, Control> defCtrlItems = defaultControls.getItems();
        
        Control testItem = defCtrlItems.get("d:test");
        assertNotNull(testItem);
        
        // We want the dependencies as arrays as these are more JS-friendly than
        // Lists, but I'll compare the expected values as Lists.
        String[] expectedCssDependencies = new String[]{"/css/path/1/override", "/css/path/2"};
        String[] expectedJsDependencies = new String[]{"/js/path/1", "/js/path/2/override"};

        assertEquals(Arrays.asList(expectedCssDependencies), Arrays.asList(testItem.getCssDependencies()));
        assertEquals(Arrays.asList(expectedJsDependencies), Arrays.asList(testItem.getJsDependencies()));
    }

    /**
     * This test checks that the expected JS and CSS resources are available for a
     * control defined on a field.
     */
    @Override
    public void off_testGetDependenciesForFieldControl() throws Exception
    {
        Control nameControl
            = myExampleDefaultForm.getFields().get("cm:name").getControl();
        
        // We want the dependencies as arrays as these are more JS-friendly than
        // Lists, but I'll compare the expected values as Lists.
        String[] expectedCssDependencies = new String[]{"/css/path/f1", "/css/path/f2/override"};
        String[] expectedJsDependencies = new String[]{"/js/path/f1/override", "/js/path/f2"};

        assertEquals(Arrays.asList(expectedCssDependencies), Arrays.asList(nameControl.getCssDependencies()));
        assertEquals(Arrays.asList(expectedJsDependencies), Arrays.asList(nameControl.getJsDependencies()));
    }
}
