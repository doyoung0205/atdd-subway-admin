package nextstep.subway.line.domain;

import nextstep.subway.common.domain.BaseEntity;
import nextstep.subway.common.exception.CustomException;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;

@Entity
public class Section extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

    @ManyToOne
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @ManyToOne
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    private Distance distance;

    protected Section() {
    }

    public Section(Line line, Station upStation, Station downStation, long distance) {
        validate(line, upStation, downStation);
        this.line = line;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = new Distance(distance);
    }

    private void validate(Line line, Station upStation, Station downStation) {
        if (line == null) {
            throw new CustomException("구간의 노선 정보가 포함되어야합니다.");
        }
        if (upStation == null && downStation == null) {
            throw new CustomException("구간의 상행 또는 하행역 정보는 추가되어야합니다.");
        }
    }

    public void updateUpStation(Section section) {
        if (this.distance.isLessThanEqual(section.distance)) {
            throw new CustomException("역과 역 사이의 거리보다 좁은 거리를 입력해주세요");
        }
        this.upStation = section.downStation;
        this.distance = new Distance(this.distance.minus(section.distance));
    }

    public void updateDownStation(Section section) {
        if (this.distance.isLessThanEqual(section.distance)) {
            throw new CustomException("역과 역 사이의 거리보다 좁은 거리를 입력해주세요");
        }
        this.downStation = section.upStation;
        this.distance = new Distance(this.distance.minus(section.distance));
    }

    public boolean hasUpSection(Station upStation) {
        return this.downStation == upStation;
    }

    public boolean hasDownSection(Station downStation) {
        return this.upStation == downStation;
    }

    public Long getId() {
        return id;
    }

    public Line getLine() {
        return line;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }
}