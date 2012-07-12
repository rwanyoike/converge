/*
 * Copyright (C) 2010 - 2012 Interactive Media Management
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
import dk.i2m.converge.core.content.ContentItem;
import dk.i2m.converge.core.content.NewsItemMediaAttachment;
import dk.i2m.converge.core.metadata.Concept;
import dk.i2m.converge.core.metadata.Subject;
import dk.i2m.converge.core.security.UserAccount;
import dk.i2m.converge.core.utils.BeanComparator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;

/**
 * Persisted model represents a digital asset belonging to a {@link Catalogue}
 * A {@link MediaItem} could be an image, audio recording, video clip or
 * document.
 *
 * @author Allan Lykke Christensen
 */
@Entity()
@Table(name = "media_item")
@DiscriminatorValue("media_item")
@NamedQueries({
    @NamedQuery(name = MediaItem.FIND_BY_CATALOGUE, query = "SELECT DISTINCT m FROM MediaItem m WHERE m.catalogue = :catalogue ORDER BY m.id ASC"),
    @NamedQuery(name = MediaItem.FIND_BY_STATUS, query = "SELECT DISTINCT m FROM MediaItem m WHERE m.status = :status ORDER BY m.updated DESC"),
    @NamedQuery(name = MediaItem.FIND_BY_OWNER, query = "SELECT DISTINCT m FROM MediaItem m WHERE m.owner = :owner ORDER BY m.id DESC"),
    @NamedQuery(name = MediaItem.FIND_CURRENT_AS_OWNER, query = "SELECT DISTINCT m FROM MediaItem m JOIN m.catalogue c WHERE c = :mediaRepository AND m.owner = :user AND m.status <> dk.i2m.converge.core.content.catalogue.MediaItemStatus.APPROVED AND m.status <> dk.i2m.converge.core.content.catalogue.MediaItemStatus.REJECTED ORDER BY m.updated DESC"),
    @NamedQuery(name = MediaItem.FIND_BY_OWNER_AND_STATUS, query = "SELECT  DISTINCT m FROM MediaItem m JOIN m.catalogue c WHERE c = :catalogue AND m.owner = :user AND m.status = :status ORDER BY m.updated DESC"),
    @NamedQuery(name = MediaItem.COUNT_BY_USER_AND_CATALOGUE_AND_WORKFLOW_STATE, query = "SELECT COUNT(DISTINCT n) FROM ContentItem AS ci, MediaItem AS n JOIN n.actors AS a WHERE n.id=ci.id AND n.catalogue = :" + MediaItem.PARAM_CATALOGUE + " AND n.currentState = :" + MediaItem.PARAM_WORKFLOW_STATE + " AND (( a.user = :"+MediaItem.PARAM_USER+") OR (n.currentState.permission = dk.i2m.converge.core.workflow.WorkflowStatePermission.GROUP AND :"+MediaItem.PARAM_USER+" MEMBER OF n.currentState.actorRole.userAccounts))"),
    @NamedQuery(name = MediaItem.COUNT_BY_USER_AND_CATALOGUE, query = "SELECT COUNT(DISTINCT n) FROM ContentItem AS ci, MediaItem AS n JOIN n.actors AS a WHERE n.id=ci.id AND n.catalogue = :" + MediaItem.PARAM_CATALOGUE + " AND (( a.user = :"+MediaItem.PARAM_USER+") OR (n.currentState.permission = dk.i2m.converge.core.workflow.WorkflowStatePermission.GROUP AND :"+MediaItem.PARAM_USER+" MEMBER OF n.currentState.actorRole.userAccounts))")
})
public class MediaItem extends ContentItem {

    public static final String FIND_BY_CATALOGUE = "MediaItem.FindByCatalogue";

    public static final String FIND_BY_STATUS = "MediaItem.FindByStatus";

    public static final String FIND_BY_OWNER = "MediaItem.FindByOwner";

    public static final String FIND_CURRENT_AS_OWNER = "MediaItem.FindCurrentAsOwner";

    public static final String FIND_BY_OWNER_AND_STATUS = "MediaItem.FindByOwnerAndStatus";
    
    /** Query for counting the results from the {@link #FIND_BY_USER_AND_CATALOGUE_AND_WORKFLOW_STATE} query. */
    public static final String COUNT_BY_USER_AND_CATALOGUE_AND_WORKFLOW_STATE = "MediaItem.CountByUserAndCatalogueAndWorkflowState";
        
    /** Query for counting the results from the {@link #FIND_BY_USER_AND_CATALOGUE_AND_WORKFLOW_STATE} query. */
    public static final String COUNT_BY_USER_AND_CATALOGUE = "MediaItem.CountByUserAndCatalogue";
    
    /** Query parameter used to specify the user. */
    public static final String PARAM_USER = "user";
    
    /** Query parameter used to specify the catalogue. */
    public static final String PARAM_CATALOGUE = "catalogue";
    
    /** Query parameter used to specify the workflow state. */
    public static final String PARAM_WORKFLOW_STATE = "workflowState";
    
    private static final long serialVersionUID = 2L;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "media_item_concept",
    joinColumns = {@JoinColumn(referencedColumnName = "id", name = "media_item_id", nullable = false)},
    inverseJoinColumns = {@JoinColumn(referencedColumnName = "id", name = "concept_id", nullable = false)})
    private List<Concept> concepts = new ArrayList<Concept>();

    @OneToMany(mappedBy = "mediaItem", fetch = FetchType.EAGER)
    private List<MediaItemRendition> renditions = new ArrayList<MediaItemRendition>();

    @Column(name = "held")
    private boolean held = false;

    @javax.persistence.Version
    @Column(name = "opt_lock")
    private int versionIdentifier;

    /** {@link List} of placements of this {@link MediaItem} in {@link Channel}s. */
    @OneToMany(mappedBy = "mediaItem")
    private List<MediaItemPlacement> placements =
            new ArrayList<MediaItemPlacement>();

    /**
     * Creates a new instance of {@link MediaItem}.
     */
    public MediaItem() {
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

    /**
     * Property containing a {@link List} of placements of the {@link MediaItem}
     * in {@link Channel}s.
     * 
     * @return {@link List} of placements of the {@link MediaItem} in
     *         {@link Channel}s
     */
    public List<MediaItemPlacement> getPlacements() {
        return placements;
    }

    /**
     * Property containing a {@link List} of placements of the {@link MediaItem}
     * in {@link Channel}s.
     * 
     * @param placements
     *          {@link List} of placements of the {@link MediaItem} in
     *          {@link Channel}s
     */
    public void setPlacements(List<MediaItemPlacement> placements) {
        this.placements = placements;
    }
    
    public List<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<Concept> concepts) {
        this.concepts = concepts;
    }

    /**
     * Gets a {@link List} of {@link Subject}s. The {@link List} is derived
     * from the list of {@link Concept}s attached to the {@link MediaItem}.
     *
     * @return {@link List} of derived {@link Subject}s from the {@link List} of
     *         attached {@link Concept}s
     */
    public List<Subject> getSubjects() {
        List<Subject> subjects = new ArrayList<Subject>();
        for (Concept c : getConcepts()) {
            if (c instanceof Subject) {
                subjects.add((Subject) c);
            }
        }
        return subjects;
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
     * Determines if the {@link MediaItem} is being held. That is, if usage of
     * this item should be prevented.
     *
     * @return {@code true} if the {@link MediaItem} is held, otherwise
     *         {@code false}
     */
    public boolean isHeld() {
        return held;
    }

    /**
     * Set the "Held" status of the {@link MediaItem} is being held. That is, if
     * usage of this item should be prevented.
     * 
     * @param held 
     *          {@code true} if the {@link MediaItem} is held, otherwise
     *          {@code false}
     */
    public void setHeld(boolean held) {
        this.held = held;
    }

    /**
     * Gets a {@link List} of {@link Rendition}s not attached
     * to this {@link MediaItem}.
     *
     * @return {@link List} of {@link Rendition}s not attached to the 
     *         {@link MediaItem}
     */
    public List<Rendition> getMissingRenditions() {
        List<Rendition> missing = new ArrayList<Rendition>();
        List<Rendition> catalogueRenditions = getCatalogue().getRenditions();

        for (Rendition rendition : catalogueRenditions) {
            boolean available = false;
            for (MediaItemRendition mir : getRenditions()) {
                if (mir.getRendition().equals(rendition)) {
                    available = true;
                }
            }
            if (!available) {
                missing.add(rendition);
            }
        }

        // Sort the renditions by the label
        Collections.sort(missing, new BeanComparator("label"));

        return missing;
    }

    /**
     * Gets a {@link List} of {@link Rendition}s for this
     * {@link MediaItem}.
     * <p/>
     * @return {@link List} of {@link Rendition}s for this
     *         {@link MediaItem}
     */
    public List<MediaItemRendition> getRenditions() {
        Collections.sort(renditions, new BeanComparator("rendition.label"));
        return renditions;
    }

    /**
     * Sets a {@link List} of {@link Rendition}s for this
     * {@link MediaItem}.
     *
     * @param renditions
     *          {@link List} of {@link Rendition}s for this {@link MediaItem}
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
     * <p/>
     * @return {@link MediaItemRendition} to be used for preview, or 
     *         {@code null} if no {@link MediaItemRendition} is suitable for 
     *         displaying a preview.
     */
    public MediaItemRendition getPreview() {
        if (isPreviewAvailable()) {
            try {
                return findRendition(catalogue.getPreviewRendition());
            } catch (RenditionNotFoundException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Determines if a preview is available of the {@link MediaItem}.
     * <p/>
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
     * <p/>
     * @return {@link MediaItemRendition} representing the original
     *         {@link MediaItem}, or {@code null} if no
     *         {@link MediaItemRendition} is suitable for displaying a preview.
     */
    public MediaItemRendition getOriginal() {
        if (isOriginalAvailable()) {
            try {
                MediaItemRendition original = findRendition(catalogue.
                        getOriginalRendition());
                return original;
            } catch (RenditionNotFoundException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Determines if a preview is available of the {@link MediaItem}.
     * <p/>
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
     * <p/>
     * @param rendition
     *          {@link Rendition} to find
     * @return {@link MediaItemRendition} of the given {@link Rendition}
     * @throws RenditionNotFoundException 
     *          If non of the {@link MediaItemRendition} matched the given 
     *          {@link Rendition}
     */
    public MediaItemRendition findRendition(Rendition rendition) throws
            RenditionNotFoundException {
        return findRendition(rendition.getName());
    }

    /**
     * Finds the {@link MediaItemRendition} of a given {@link Rendition}.
     *
     * @param rendition 
     *          {@link Rendition} to find
     * @return {@link MediaItemRendition} of the given {@link Rendition}
     * @throws RenditionNotFoundException 
     *          If non of the {@link MediaItemRendition} matched the given
     *          {@link Rendition}
     */
    public MediaItemRendition findRendition(String rendition) throws
            RenditionNotFoundException {

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
     * @return {@code true} if one or more {@link Rendition}s have been 
     *         attached, otherwise {@code false}
     */
    public boolean isRenditionsAttached() {
        if (getRenditions() == null || getRenditions().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determines if a given {@link Rendition} is attached to the 
     * {@link MediaItem}.
     *
     * @param rendition
     *          Name of the {@link Rendition}
     * @return {@code true} if the given {@link Rendition} is attached,
     *         otherwise {@code false}
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
        if (getId() != other.getId()
                && (getId() == null || !getId().equals(other.getId()))) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc } */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    /** {@inheritDoc } */
    @Override
    public String toString() {
        return getClass().getName() + "[id=" + getId() + "]";
    }
}
