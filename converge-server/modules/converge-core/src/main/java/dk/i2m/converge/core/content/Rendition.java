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
package dk.i2m.converge.core.content;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.Table;

/**
 * Entity representing a label that can be attached to a media item to identify
 * its purpose.
 *
 * @author Allan Lykke Christensen
 */
@Entity
@Table(name = "rendition")
@NamedQueries({})
public class Rendition implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;

    @Column(name = "name")
    private String name;

    @Column(name = "description") @Lob
    private String description;

    /**
     * Creates a new {@link Rendtion}.
     */
    public Rendition() {
        this.label = "";
        this.name = "";
        this.description = null;
    }

    /**
     * Gets the unique identifier of the {@link Rendition}.
     * 
     * @return Unique identifier of the {@link Rendition}
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the {@link Rendition}.
     * 
     * @param id
     *          Unique identifier of the {@link Rendition}
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the label of the {@link Rendition}. The label is used
     * to display a "user-friendly" version of the {@link Rendition#name}.
     * 
     * @return Label of the {@link Rendition}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of the {@link Rendition}.
     * 
     * @param label 
     *          Label of the {@link Rendition}
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the technical name of the {@link Rendition}. The technical 
     * name is typically a short textual identifier used by plug-ins
     * to identify items of this particular type.
     * 
     * @return Name of the {@link Rendition}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the {@link Rendition}.
     * 
     * @param name 
     *          Name of the {@link Rendition}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description and purpose of the {@link Rendition}.
     * 
     * @return Description of the {@link Rendition}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description and purpose of the {@link Rendition}.
     * 
     * @param description 
     *          Description of the {@link Rendition}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rendition other = (Rendition) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + id + "]";
    }
}
