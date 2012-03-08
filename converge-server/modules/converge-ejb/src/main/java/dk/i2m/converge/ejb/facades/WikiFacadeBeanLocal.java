/*
 * Copyright (C) 2012 Interactive Media Management
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.i2m.converge.ejb.facades;

import dk.i2m.converge.core.wiki.Page;
import dk.i2m.converge.core.DataNotFoundException;
import java.util.List;
import javax.ejb.Local;

/**
 * Local interface for the {@link WikiFacadeBean}.
 *
 * @author Allan Lykke Christensen
 */
@Local
public interface WikiFacadeBeanLocal {

    /**
     * Finds all the pages that should be displayed on the sub-menu.
     * <p/>
     * @return {@link List} of {@link Page}s that should be displayed on the sub-menu.
     */
    List<Page> findSubmenuPages();

    Page update(Page Page);

    Page create(Page page);
    
    void deletePageById(Long id);
    
    Page findPageById(Long id) throws DataNotFoundException;
    
    Page findPageByTitle(String title) throws DataNotFoundException;
    
}