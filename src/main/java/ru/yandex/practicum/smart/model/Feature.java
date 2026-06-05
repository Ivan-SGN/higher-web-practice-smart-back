package ru.yandex.practicum.smart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.smart.model.Chat;
import ru.yandex.practicum.smart.model.enums.FeatureStatus;
import ru.yandex.practicum.smart.model.enums.FeatureType;

import java.time.LocalDateTime;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}