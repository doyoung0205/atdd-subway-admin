package nextstep.subway.line.domain;

import nextstep.subway.line.exception.LastLineSectionDeleteException;
import nextstep.subway.line.exception.LineAlreadyBothRegisteredException;
import nextstep.subway.line.exception.NotExistEitherStationException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.exception.NotFoundStationByIdException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Embeddable
public class Sections {
    private static final String NOT_NULL_ERROR_MESSAGE = "종점역은 빈값이 될 수 없습니다.";
    private static final String NOT_CONTAINS_STATION_ERROR_MESSAGE = "해당 구간의 등록되어 있지 않은 지하철 역 입니다.";
    private static final int DELETE_SECTION_MIN_SIZE = 1;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lineId", foreignKey = @ForeignKey(name = "fk_section_to_line"))
    private final List<Section> sections = new ArrayList<>();

    protected Sections() {
    }

    private Sections(Section section) {
        validateNotNull(section);
        this.sections.add(section);
    }

    private void validateNotNull(Section section) {
        if (Objects.isNull(section)) {
            throw new IllegalArgumentException(NOT_NULL_ERROR_MESSAGE);
        }
    }

    public void addSection(final Section section) {
        final Station upStation = section.getUpStation();
        final Station downStation = section.getDownStation();

        final boolean isUpStationExisted = isStationExist(upStation);
        final boolean isDownStationExisted = isStationExist(downStation);

        validateStationExist(isDownStationExisted, isUpStationExisted);

        if (isDownStationExisted) {
            updateDownStation(section);
        }

        if (isUpStationExisted) {
            updateUpStation(section);
        }

        this.sections.add(section);
    }

    public void removeStation(Station station) {
        validateRemoveMinSectionSize();

        final Optional<Section> upSection = findDownSectionByStation(station);
        final Optional<Section> downSection = findUpSectionByStation(station);

        final boolean isUpSectionExist = upSection.isPresent();
        final boolean isDownSectionExist = downSection.isPresent();

        validateSectionExist(isUpSectionExist, isDownSectionExist);

        if (isUpSectionExist && isDownSectionExist) {
            this.sections.add(Section.of(upSection.get(), downSection.get()));
        }

        upSection.ifPresent(this.sections::remove);
        downSection.ifPresent(this.sections::remove);
    }

    private void validateSectionExist(boolean isUpSectionExist, boolean isDownSectionExist) {
        if (!isUpSectionExist && !isDownSectionExist) {
            throw new NotFoundStationByIdException(NOT_CONTAINS_STATION_ERROR_MESSAGE);
        }
    }

    private void validateRemoveMinSectionSize() {
        if (this.sections.size() <= DELETE_SECTION_MIN_SIZE) {
            throw new LastLineSectionDeleteException();
        }
    }

    private Optional<Section> findUpSectionByStation(Station station) {
        return this.sections.stream()
                .filter(it -> it.getDownStation().equals(station))
                .findAny();
    }

    private Optional<Section> findDownSectionByStation(Station station) {
        return this.sections.stream()
                .filter(it -> it.getUpStation().equals(station))
                .findAny();
    }

    private void updateUpStation(Section section) {
        sections.stream()
                .filter(it -> it.getUpStation() == section.getUpStation())
                .findAny()
                .ifPresent(it -> it.updateUpStation(section.getDownStation(), section.getDistance()));
    }

    private void updateDownStation(Section section) {
        sections.stream()
                .filter(it -> it.getDownStation() == section.getDownStation())
                .findAny()
                .ifPresent(it -> it.updateDownStation(section.getUpStation(), section.getDistance()));
    }

    private boolean isStationExist(final Station station) {
        return this.sections.stream()
                .anyMatch(it -> it.matchAnyStation(station));
    }

    private void validateStationExist(boolean isDownStationExisted, boolean isUpStationExisted) {
        if (isDownStationExisted && isUpStationExisted) {
            throw new LineAlreadyBothRegisteredException();
        }

        if (!isDownStationExisted && !isUpStationExisted) {
            throw new NotExistEitherStationException();
        }
    }

    public Sections(List<Section> sections) {
        this.sections.addAll(sections);
    }

    public static Sections of(Section section) {
        return new Sections(section);
    }

    public static Sections of(List<Section> sections) {
        return new Sections(sections);
    }

    public List<Station> getStationsOrderByUptoDown() {
        return LineStationUpToDownSortUtils.sort(this.sections);
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public String toString() {
        return sections.toString();
    }
}
