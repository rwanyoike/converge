/*
 * Copyright (C) 2010 Interactive Media Management
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
package dk.i2m.converge.core.content.catalogue;

import dk.i2m.converge.core.content.Assignment;
import dk.i2m.converge.core.metadata.Concept;
import dk.i2m.converge.core.security.UserAccount;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * Persisted model represents a media item belonging to a
 * {@link MediaRepository}. A {@link MediaItem} could be an
 * image, audio recording or video clip.
 *
 * @author Allan Lykke Christensen
 */
@Entity()
@Table(name = "media_item")
@NamedQueries({
    @NamedQuery(name = MediaItem.FIND_BY_STATUS, query = "SELECT DISTINCT m FROM MediaItem m WHERE m.status = :status ORDER BY m.updated DESC"),
    @NamedQuery(name = MediaItem.FIND_BY_OWNER, query = "SELECT DISTINCT m FROM MediaItem m WHERE m.owner = :owner ORDER BY m.id DESC"),
    @NamedQuery(name = MediaItem.FIND_CURRENT_AS_OWNER, query = "SELECT DISTINCT m FROM MediaItem m JOIN m.catalogue c WHERE c = :mediaRepository AND m.status <> dk.i2m.converge.core.content.catalogue.MediaItemStatus.APPROVED AND m.status <> dk.i2m.converge.core.content.catalogue.MediaItemStatus.REJECTED AND m.owner = :user ORDER BY m.updated DESC"),
    @NamedQuery(name = MediaItem.FIND_CURRENT_AS_EDITOR, query = "SELECT DISTINCT m FROM MediaItem m JOIN m.catalogue c WHERE c = :mediaRepository AND m.status = dk.i2m.converge.core.content.catalogue.MediaItemStatus.SUBMITTED AND (:user MEMBER OF c.editorRole.userAccounts) ORDER BY m.updated DESC"),
    @NamedQuery(name = MediaItem.FIND_BY_OWNER_AND_STATUS, query = "SELECT  DISTINCT m FROM MediaItem m JOIN m.catalogue c WHERE c = :mediaRepository AND (m.owner = :user OR :user MEMBER OF c.editorRole.userAccounts) AND m.status = :status ORDER BY m.updated DESC")
})
public class MediaItem implements Serializable {

    public static final String FIND_BY_STATUS = "MediaItem.FindByStatus";

    public static final String FIND_BY_OWNER = "MediaItem.FindByOwner";

    public static final String FIND_CURRENT_AS_OWNER = "MediaItem.FindCurrentAsOwner";

    public static final String FIND_CURRENT_AS_EDITOR = "MediaItem.FindCurrentAsEditor";

    public static final String FIND_BY_OWNER_AND_STATUS = "MediaItem.FindByOwnerAndStatus";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "catalogue_id")
    private Catalogue catalogue;

    @Column(name = "byline") @Lob
    private String byLine = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MediaItemStatus status = MediaItemStatus.UNSUBMITTED;

    @ManyToOne
    @JoinColumn(name = "owner")
    private UserAccount owner;

    @Column(name = "editorial_note")
    private String editorialNote = "";

    @Column(name = "title") @Lob
    private String title = "";

    @Column(name = "description") @Lob
    private String description = "";

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "media_date")
    private Calendar mediaDate = Calendar.getInstance();

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "created")
    private Calendar created = Calendar.getInstance();

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "updated")
    private Calendar updated = Calendar.getInstance();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "media_item_concept",
    joinColumns = {@JoinColumn(referencedColumnName = "id", name = "media_item_id", nullable = false)},
    inverseJoinColumns = {@JoinColumn(referencedColumnName = "id", name = "concept_id", nullable = false)})
    private List<Concept> concepts = new ArrayList<Concept>();

    @OneToMany(mappedBy = "mediaItem")
    private List<MediaItemRendition> renditions = new ArrayList<MediaItemRendition>();

    @javax.persistence.Version
    @Column(name = "opt_lock")
    private int versionIdentifier;

    /**
     * Creates a new instance of {@link MediaItem}.
     */
    public MediaItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public String getEditorialNote() {
        return editorialNote;
    }

    public void setEditorialNote(String editorialNote) {
        this.editorialNote = editorialNote;
    }

    public Catalogue getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    public List<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<Concept> concepts) {
        this.concepts = concepts;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Calendar getMediaDate() {
        return mediaDate;
    }

    public void setMediaDate(Calendar mediaDate) {
        this.mediaDate = mediaDate;
    }

    public Calendar getUpdated() {
        return updated;
    }

    public void setUpdated(Calendar updated) {
        this.updated = updated;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public MediaItemStatus getStatus() {
        return status;
    }

    public void setStatus(MediaItemStatus status) {
        this.status = status;
    }

    public String getByLine() {
        return byLine;
    }

    public void setByLine(String byLine) {
        this.byLine = byLine;
    }

    /**
     * Gets a {@link List} of {@link Rendition}s for this
     * {@link MediaItem}.
     * 
     * @return {@link List} of {@link Rendition}s for this
     *         {@link MediaItem}
     */
    public List<MediaItemRendition> getRenditions() {
        return renditions;
    }

    /**
     * Sets a {@link List} of {@link Rendition}s for this
     * {@link MediaItem}.
     * 
     * @param renditions 
     *          {@link List} of {@link Rendition}s for this
     *          {@link MediaItem}
     */
    public void setRenditions(List<MediaItemRendition> renditions) {
        this.renditions = renditions;
    }

    public int getVersionIdentifier() {
        return versionIdentifier;
    }

    public void setVersionIdentifier(int versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    /**
     * Gets the {@link MediaItemRendition} that should be used to show
     * a preview of the {@link MediaItem}. The preview is based on the
     * {@link Rendition} selected for preview on the {@link Catalogue}.
     * 
     * @return {@link MediaItemRendition} to be used for preview, or {@code null}
     *         if no {@link MediaItemRendition} is suitable for displaying
     *         a preview.
     */
    public MediaItemRendition getPreview() {
        if (isPreviewAvailable()) {
            try {
                MediaItemRendition preview = findRendition(catalogue.getPreviewRendition());
                return preview;
            } catch (RenditionNotFoundException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Determines if a preview is available of the {@link MediaItem}.
     * 
     * @return {@code true} if a preview is available, otherwise {@link code false}
     */
    public boolean isPreviewAvailable() {
        if (catalogue == null || catalogue.getPreviewRendition() == null) {
            return false;
        }

        try {
            findRendition(catalogue.getPreviewRendition());
            return true;
        } catch (RenditionNotFoundException ex) {
            return false;
        }
    }

    /**
     * Gets the {@link MediaItemRendition} that represents the original
     * {@link MediaItem}. The preview is based on the {@link Rendition} 
     * selected as the original on the {@link Catalogue}.
     * 
     * @return {@link MediaItemRendition} representing the original
     *         {@link MediaItem}, or {@code null} if no 
     *         {@link MediaItemRendition} is suitable for displaying
     *         a preview.
     */
    public MediaItemRendition getOriginal() {
        if (isOriginalAvailable()) {
            try {
                MediaItemRendition original = findRendition(catalogue.getOriginalRendition());
                return original;
            } catch (RenditionNotFoundException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Determines if a preview is available of the {@link MediaItem}.
     * 
     * @return {@code true} if a preview is available, otherwise {@link code false}
     */
    public boolean isOriginalAvailable() {
        if (catalogue == null || catalogue.getOriginalRendition() == null) {
            return false;
        }

        try {
            findRendition(catalogue.getOriginalRendition());
            return true;
        } catch (RenditionNotFoundException ex) {
            return false;
        }
    }

    /**
     * Finds the {@link MediaItemRendition} of a given {@link Rendition}.
     * 
     * @param rendition
     *          {@link Rendition} to find
     * @return {@link MediaItemRendition} of the given {@link Rendition}
     * @throws RenditionNotFoundException 
     *          If non of the {@link MediaItemRendition} matched the
     *          given {@link Rendition}
     */
    public MediaItemRendition findRendition(Rendition rendition) throws RenditionNotFoundException {
        return findRendition(rendition.getName());
    }

    /**
     * Finds the {@link MediaItemRendition} of a given {@link Rendition}.
     * 
     * @param rendition
     *          {@link Rendition} to find
     * @return {@link MediaItemRendition} of the given {@link Rendition}
     * @throws RenditionNotFoundException 
     *          If non of the {@link MediaItemRendition} matched the
     *          given {@link Rendition}
     */
    public MediaItemRendition findRendition(String rendition) throws RenditionNotFoundException {
        for (MediaItemRendition mir : getRenditions()) {
            if (mir.getRendition().getName().equalsIgnoreCase(rendition)) {
                return mir;
            }
        }
        throw new RenditionNotFoundException();
    }

    /**
     * Determines if any {@link Rendition}s have been attached.
     * 
     * @return {@code true} if one or more {@link Rendition}s 
     *         have been attached, otherwise {@code false}
     */

    public boolean isRenditionsAttached() {
        if (getRenditions() == null || getRenditions().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Determines if a given {@link Rendition} is attached
     * to the {@link MediaItem}.
     * 
     * @param rendition
     *          Name of the {@link Rendition}
     * @return {@code true} if the given {@link Rendition} is
     *         attached, otherwise {@code false}
     */
    public boolean isRenditionAttached(String rendition) {
        try {
            findRendition(rendition);
            return true;
        } catch (RenditionNotFoundException ex) {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MediaItem other = (MediaItem) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}