package org.example.lab.model;
import jakarta.persistence.*;

@Entity
@Table(name="import_history")
public class ImportHistory {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id", nullable=false)
    public AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    public ImportStatus status;

    @Column(name="added_count")
    public Integer addedCount;

    @Column(name="error_message")
    public String errorMessage;
}
