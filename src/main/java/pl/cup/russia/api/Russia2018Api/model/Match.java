package pl.cup.russia.api.Russia2018Api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.cup.russia.api.Russia2018Api.dto.Result;
import pl.cup.russia.api.Russia2018Api.enums.WinnerSide;
import pl.cup.russia.api.Russia2018Api.external.api.model.ApiEvent;
import pl.cup.russia.api.Russia2018Api.util.BetValidator;

import java.time.LocalDate;
import java.time.LocalTime;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static pl.cup.russia.api.Russia2018Api.enums.MatchResult.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "matches")
public class Match {

	@Id
	private String id;

	private Integer leagueApiId;

	private String leagueApiName;

	private Integer matchApiId;

	private LocalDate date;

	private LocalTime time;

	private String status;

	private Boolean live;

	private String hometeamName;

	private Integer hometeamScore;

	private String awayteamName;

	private Integer awayteamScore;

	private Integer hometeamHalftimeScore;

	private Integer awayteamHalftimeScore;

	private Integer hometeamExtraScore;

	private Integer awayteamExtraScore;

	private Integer hometeamPenaltyScore;

	private Integer awayteamPenaltyScore;

	@Transient
	private Boolean canBetMatch;

	public Boolean getCanBetMatch() {
		return BetValidator.canBetMatch(this.date, this.time);
	}

	// rest of data if it will be time for it

	public Match(ApiEvent apiEvent) {
		this.leagueApiId = apiEvent.getLeagueId();
		this.leagueApiName = apiEvent.getLeagueName();
		this.matchApiId = apiEvent.getMatchId();
		this.date = apiEvent.getMatchDate();
		this.time = apiEvent.getMatchTime();
		this.status = apiEvent.getMatchStatus();
		this.hometeamName = apiEvent.getMatchHometeamName();
		this.hometeamScore = apiEvent.getMatchHometeamScore();
		this.awayteamName = apiEvent.getMatchAwayteamName();
		this.awayteamScore = apiEvent.getMatchAwayteamScore();
		this.hometeamHalftimeScore = apiEvent.getMatchHometeamHalftimeScore();
		this.awayteamHalftimeScore = apiEvent.getMatchAwayteamHalftimeScore();
		this.hometeamExtraScore = apiEvent.getMatchHometeamExtraScore();
		this.awayteamExtraScore = apiEvent.getMatchAwayteamExtraScore();
		this.hometeamPenaltyScore = apiEvent.getMatchHometeamPenaltyScore();
		this.awayteamPenaltyScore = apiEvent.getMatchAwayteamPenaltyScore();
	}

	public Boolean isExtraTime() {
		if (nonNull(this.hometeamExtraScore) && nonNull(this.awayteamExtraScore))
			return TRUE;
		return FALSE;
	}

	public Boolean isPenalties() {
		if (nonNull(this.hometeamPenaltyScore) && nonNull(this.awayteamPenaltyScore))
			return TRUE;
		return FALSE;
	}

	@JsonIgnore // TODO: ignore for now, jackson mapping to repair in this model
	public Result getResult() {
		if (hometeamScore.equals(awayteamScore))
			return new Result(DRAW);

		String winnerName;
		if (isPenalties()) {
			winnerName = getWinnerName(hometeamPenaltyScore, awayteamPenaltyScore);
			return new Result(WIN_PENALTIES, winnerName, getWinnerSide(winnerName));
		} else if (isExtraTime()) {
			winnerName = getWinnerName(hometeamExtraScore, awayteamExtraScore);
			return new Result(WIN_EXTRA_TIME, winnerName, getWinnerSide(winnerName));
		} else {
			winnerName = getWinnerName(hometeamScore, awayteamScore);
			return new Result(WIN_REGULAR_TIME, winnerName, getWinnerSide(winnerName));
		}

	}

	private String getWinnerName(Integer homeScore, Integer awayScore) {
		return (homeScore > awayScore) ? hometeamName : awayteamName;
	}

	private WinnerSide getWinnerSide(String winnerName) {
		if (this.getHometeamName().equals(winnerName))
			return WinnerSide.HOME;

		return WinnerSide.AWAY;
	}

}
