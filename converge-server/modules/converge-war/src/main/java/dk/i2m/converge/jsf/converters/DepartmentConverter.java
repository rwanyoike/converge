/*
 *  Copyright (C) 2010 Interactive Media Management
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.i2m.converge.jsf.converters;

import dk.i2m.converge.core.workflow.Department;
import dk.i2m.converge.ejb.facades.OutletFacadeLocal;
import dk.i2m.converge.core.DataNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * JSF {@link Converter} for {@link Department} objects.
 *
 * @author Allan Lykke Christensen
 */
public class DepartmentConverter implements Converter {

    private static final Logger log = Logger.getLogger(DepartmentConverter.class.
            getName());

    private OutletFacadeLocal outletFacade;

    public DepartmentConverter(OutletFacadeLocal outletFacade) {
        this.outletFacade = outletFacade;
    }

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent component,
            String value) {
        try {
            if (value == null) {
                return null;
            }
            return outletFacade.findDepartmentById(Long.valueOf(value));

        } catch (DataNotFoundException ex) {
            log.log(Level.WARNING, "No department matching [{0}]", value);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent component,
            Object value) {
        if (value == null) {
            return "";
        } else {
            Department department = (Department) value;
            return String.valueOf(department.getId());
        }
    }
}
