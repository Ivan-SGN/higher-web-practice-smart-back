package ru.yandex.practicum.smart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;
import ru.yandex.practicum.smart.model.converter.StringListConverter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "features")
@Getter
@Setter
@NoArgsConstructor
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Enumerated(EnumType.STRING)
    private FeatureType type;

    private String description;

    private String content;

    @Enumerated(EnumType.STRING)
    private FeatureStatus status;

    @Convert(converter = StringListConverter.class)
    @Column(name = "parameters")
    private List<String> parameters;

    @Convert(converter = StringListConverter.class)
    @Column(name = "results")
    private List<String> results;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}