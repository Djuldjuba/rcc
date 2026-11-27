package io.student.rcc.museum.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "museum")
public class MuseumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "city")
    private String city;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column(name = "country_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID countryId;

    public void setPhotoFromString(String photoStr) {
        this.photo = (photoStr == null) ? null : photoStr.getBytes(StandardCharsets.UTF_8);
    }

    public String getPhotoAsString() {
        return (this.photo == null) ? null : new String(this.photo, StandardCharsets.UTF_8);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MuseumEntity that = (MuseumEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}