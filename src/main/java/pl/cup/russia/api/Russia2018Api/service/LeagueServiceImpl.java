package pl.cup.russia.api.Russia2018Api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import pl.cup.russia.api.Russia2018Api.definition.LeagueService;
import pl.cup.russia.api.Russia2018Api.external.api.definition.FootballApiService;
import pl.cup.russia.api.Russia2018Api.external.api.model.ApiLeague;
import pl.cup.russia.api.Russia2018Api.external.api.model.ApiStanding;
import pl.cup.russia.api.Russia2018Api.model.League;
import pl.cup.russia.api.Russia2018Api.model.Standing;
import pl.cup.russia.api.Russia2018Api.repository.LeagueRepository;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static java.util.stream.Collectors.toList;
import static pl.cup.russia.api.Russia2018Api.enums.DBCollections.LEAGUES;
import static pl.cup.russia.api.Russia2018Api.util.TranslationUtil.getPolishCountryNames;

@Service
public class LeagueServiceImpl implements LeagueService {

	@Autowired
	private FootballApiService apiService;

	@Autowired
	private LeagueRepository repository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void syncLeagues() {
		List<ApiLeague> apiLeagues = apiService.getApiLeagues();
		List<League> leagues = new ArrayList<>();

		for (ApiLeague apiLeague : apiLeagues) {
			League league = new League(apiLeague);
			if (league.isGroupStage()) {
				List<ApiStanding> apiStandings = apiService.getApiStandingsByLeagueId(league.getLeagueApiId());
				apiStandings.stream().forEach(std -> league.getStandings().add(new Standing(std)));
			}

			leagues.add(league);
		}

		saveAll(leagues);
	}

	@Override
	public List<League> selectLeagues() {
		return repository.findAll();
	}

	@Override
	public List<String> selectAllTeams() {
		// TODO: narrow to groups
		List<String> allTeams = mongoTemplate.getCollection(LEAGUES.getValue())
				.distinct("standings.teamName", String.class).into(new ArrayList<>());

		List<String> translatedTeams = getPolishCountryNames(allTeams);
		translatedTeams.sort(Comparator.naturalOrder());

		return translatedTeams;
	}

	@Override
	public List<String> selectTeamsByLeagueId(Integer leagueId) {
		// TODO: narrow to groups
		return mongoTemplate.getCollection(LEAGUES.getValue())
				.distinct("standings.teamName", eq("leagueApiId", leagueId), String.class).into(new ArrayList<>());
	}

	@Override
	public Map<String, List<String>> selectTeamsGroupedByLeagueName() {
		// TODO: narrow to groups
		List<League> leagues = selectLeagues();
		Map<String, List<String>> teamsByLeagueName = new LinkedHashMap<>();

		for (League league : leagues) {
			String leagueName = league.getName();
			List<String> teams = league.getStandings().stream().map(std -> std.getTeamName()).collect(toList());

			List<String> translatedTeams = getPolishCountryNames(teams);
			translatedTeams.sort(Comparator.naturalOrder());

			teamsByLeagueName.put(leagueName, translatedTeams);
		}

		return teamsByLeagueName;
	}

	@Override
	public List<League> saveAll(List<League> leagues) {
		return repository.saveAll(leagues);
	}

}
