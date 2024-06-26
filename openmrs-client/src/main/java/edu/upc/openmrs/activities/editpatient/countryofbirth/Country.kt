package edu.upc.openmrs.activities.editpatient.countryofbirth

import android.content.Context
import edu.upc.R

enum class Country(val label: Int, val flag: Int) {

    AFGHANISTAN(R.string.afghanistan, R.drawable.flag_afghanistan),
    ALBANIA(R.string.albania, R.drawable.flag_albania),
    ALGERIA(R.string.algeria, R.drawable.flag_algeria),
    AMERICAN_SAMOA(R.string.american_samoa, R.drawable.flag_united_states_of_america),
    ANDORRA(R.string.andorra, R.drawable.flag_andorra),
    ANGOLA(R.string.angola, R.drawable.flag_angola),
    ANGUILLA(R.string.anguilla, R.drawable.flag_anguilla),
    ANTARCTICA(R.string.antarctica, R.drawable.flag_antarctica),
    ANTIGUA_AND_BARBUDA(R.string.antigua_and_barbuda, R.drawable.flag_antigua_and_barbuda),
    ARGENTINA(R.string.argentina, R.drawable.flag_argentina),
    ARUBA(R.string.aruba, R.drawable.flag_aruba),
    AUSTRALIA(R.string.australia, R.drawable.flag_australia),
    AUSTRIA(R.string.austria, R.drawable.flag_austria),
    AZERBAIJAN(R.string.azerbaijan, R.drawable.flag_azerbaijan),
    BAHAMAS(R.string.bahamas, R.drawable.flag_bahamas),
    BAHRAIN(R.string.bahrain, R.drawable.flag_bahrain),
    BANGLADESH(R.string.bangladesh, R.drawable.flag_bangladesh),
    BARBADOS(R.string.barbados, R.drawable.flag_barbados),
    BELARUS(R.string.belarus, R.drawable.flag_belarus),
    BELGIUM(R.string.belgium, R.drawable.flag_belgium),
    BELIZE(R.string.belize, R.drawable.flag_belize),
    BENIN(R.string.benin, R.drawable.flag_benin),
    BERMUDA(R.string.bermuda, R.drawable.flag_bermuda),
    BHUTAN(R.string.bhutan, R.drawable.flag_bhutan),
    BOLIVIA(R.string.bolivia, R.drawable.flag_bolivia),
    BOSNIA_AND_HERZEGOVINA(R.string.bosnia_and_herzegovina, R.drawable.flag_bosnia),
    BOTSWANA(R.string.botswana, R.drawable.flag_botswana),
    BOUVET_ISLAND(R.string.bouvet_island, R.drawable.flag_norway),
    BRAZIL(R.string.brazil, R.drawable.flag_brazil),
    BRITISH_INDIAN_OCEAN_TERRITORY(R.string.british_indian_ocean_territory, R.drawable.flag_british_indian_ocean_territory),
    BRUNEI(R.string.brunei, R.drawable.flag_brunei),
    BULGARIA(R.string.bulgaria, R.drawable.flag_bulgaria),
    BURKINA_FASO(R.string.burkina_faso, R.drawable.flag_burkina_faso),
    BURUNDI(R.string.burundi, R.drawable.flag_burundi),
    CAMBODIA(R.string.cambodia, R.drawable.flag_cambodia),
    CAMEROON(R.string.cameroon, R.drawable.flag_cameroon),
    CANADA(R.string.canada, R.drawable.flag_canada),
    CAPE_VERDE(R.string.cape_verde, R.drawable.flag_cape_verde),
    CAYMAN_ISLANDS(R.string.cayman_islands, R.drawable.flag_cayman_islands),
    CENTRAL_AFRICAN_REPUBLIC(R.string.central_african_republic, R.drawable.flag_central_african_republic),
    CHAD(R.string.chad, R.drawable.flag_chad),
    CHILE(R.string.chile, R.drawable.flag_chile),
    CHINA(R.string.china, R.drawable.flag_china),
    CHRISTMAS_ISLAND(R.string.christmas_island, R.drawable.flag_christmas_island),
    COCOS_ISLAND(R.string.cocos_islands, R.drawable.flag_cocos),
    COLOMBIA(R.string.colombia, R.drawable.flag_colombia),
    COMOROS(R.string.comoros, R.drawable.flag_comoros),
    CONGO(R.string.congo, R.drawable.flag_republic_of_the_congo),
    CONGO_DEMOCRATIC_REPUBLIC(R.string.congo_democratic_republic, R.drawable.flag_democratic_republic_of_the_congo),
    COOK_ISLANDS(R.string.cook_islands, R.drawable.flag_cook_islands),
    COSTA_RICA(R.string.costa_rica, R.drawable.flag_costa_rica),
    COTE_DIVOIRE(R.string.cote_divoire, R.drawable.flag_cote_divoire),
    CROATIA(R.string.croatia, R.drawable.flag_croatia),
    CUBA(R.string.cuba, R.drawable.flag_cuba),
    CYPRUS(R.string.cyprus, R.drawable.flag_cyprus),
    CZECH_REPUBLIC(R.string.czech_republic, R.drawable.flag_czech_republic),
    DENMARK(R.string.denmark, R.drawable.flag_denmark),
    DJIBOUTI(R.string.djibouti, R.drawable.flag_djibouti),
    DOMINICA(R.string.dominica, R.drawable.flag_dominica),
    DOMINICAN_REPUBLIC(R.string.dominican_republic, R.drawable.flag_dominican_republic),
    EAST_TIMOR(R.string.east_timor, R.drawable.flag_timor_leste),
    ECUADOR(R.string.ecuador, R.drawable.flag_ecuador),
    EGYPT(R.string.egypt, R.drawable.flag_egypt),
    EL_SALVADOR(R.string.el_salvador, R.drawable.flag_el_salvador),
    EQUATORIAL_GUINEA(R.string.equatorial_guinea, R.drawable.flag_equatorial_guinea),
    ERITREA(R.string.eritrea, R.drawable.flag_eritrea),
    ESTONIA(R.string.estonia, R.drawable.flag_estonia),
    ETHIOPIA(R.string.ethiopia, R.drawable.flag_ethiopia),
    EXTERNAL_TERRITORIES_AUSTRALIA(R.string.external_territories_australia, R.drawable.flag_australia),
    FALKLAND_ISLANDS(R.string.falkland_islands, R.drawable.flag_falkland_islands),
    FAROE_ISLANDS(R.string.faroe_islands, R.drawable.flag_faroe_islands),
    FIJI(R.string.fiji, R.drawable.flag_fiji),
    FINLAND(R.string.finland, R.drawable.flag_finland),
    FRANCE(R.string.france, R.drawable.flag_france),
    FRENCH_GUIANA(R.string.french_guiana, R.drawable.flag_france),
    FRENCH_POLYNESIA(R.string.french_polynesia, R.drawable.flag_french_polynesia),
    FRENCH_SOUTHERN_TERRITORIES(R.string.french_southern_territories, R.drawable.flag_france),
    GABON(R.string.gabon, R.drawable.flag_gabon),
    GAMBIA(R.string.gambia, R.drawable.flag_gambia),
    GEORGIA(R.string.georgia, R.drawable.flag_georgia),
    GERMANY(R.string.germany, R.drawable.flag_germany),
    GHANA(R.string.ghana, R.drawable.flag_ghana),
    GIBRALTAR(R.string.gibraltar, R.drawable.flag_gibraltar),
    GREECE(R.string.greece, R.drawable.flag_greece),
    GREENLAND(R.string.greenland, R.drawable.flag_greenland),
    GRENADA(R.string.grenada, R.drawable.flag_grenada),
    GUADELOUPE(R.string.guadeloupe, R.drawable.flag_guadeloupe),
    GUAM(R.string.guam, R.drawable.flag_guam),
    GUATEMALA(R.string.guatemala, R.drawable.flag_guatemala),
    GUERNSEY_AND_ALDERNEY(R.string.guernsey_and_alderney, R.drawable.flag_guernsey),
    GUINEA(R.string.guinea, R.drawable.flag_guinea),
    GUINEA_BISSAU(R.string.guinea_bissau, R.drawable.flag_guinea_bissau),
    GUYANA(R.string.guyana, R.drawable.flag_guyana),
    HAITI(R.string.haiti, R.drawable.flag_haiti),
    HEARD_AND_MCDONALD_ISLAND(R.string.heard_and_mcdonald_islands, R.drawable.flag_australia),
    HONDURAS(R.string.honduras, R.drawable.flag_honduras),
    HONG_KONG(R.string.hong_kong_sar, R.drawable.flag_hong_kong),
    HUNGARY(R.string.hungary, R.drawable.flag_hungary),
    ICELAND(R.string.iceland, R.drawable.flag_iceland),
    INDIA(R.string.india, R.drawable.flag_india),
    INDONESIA(R.string.indonesia, R.drawable.flag_indonesia),
    IRAN(R.string.iran, R.drawable.flag_iran),
    IRAQ(R.string.iraq, R.drawable.flag_iraq),
    IRELAND(R.string.ireland, R.drawable.flag_ireland),
    ISRAEL(R.string.israel, R.drawable.flag_israel),
    ITALY(R.string.italy, R.drawable.flag_italy),
    JAMAICA(R.string.jamaica, R.drawable.flag_jamaica),
    JAPAN(R.string.japan, R.drawable.flag_japan),
    JERSEY(R.string.jersey, R.drawable.flag_jersey),
    JORDAN(R.string.jordan, R.drawable.flag_jordan),
    KAZAKHSTAN(R.string.kazakhstan, R.drawable.flag_kazakhstan),
    KENYA(R.string.kenya, R.drawable.flag_kenya),
    KIRIBATI(R.string.kiribati, R.drawable.flag_kiribati),
    NORTH_KOREA(R.string.korea_north, R.drawable.flag_north_korea),
    SOUTH_KOREA(R.string.korea_south, R.drawable.flag_south_korea),
    KUWAIT(R.string.kuwait, R.drawable.flag_kuwait),
    KYRGYZSTAN(R.string.kyrgyzstan, R.drawable.flag_kyrgyzstan),
    LAOS(R.string.laos, R.drawable.flag_laos),
    LATVIA(R.string.latvia, R.drawable.flag_latvia),
    LEBANON(R.string.lebanon, R.drawable.flag_lebanon),
    LESOTHO(R.string.lesotho, R.drawable.flag_lesotho),
    LIBERIA(R.string.liberia, R.drawable.flag_liberia),
    LIBYA(R.string.libya, R.drawable.flag_libya),
    LIECHTENSTEIN(R.string.liechtenstein, R.drawable.flag_liechtenstein),
    LITHUANIA(R.string.lithuania, R.drawable.flag_lithuania),
    LUXEMBOURG(R.string.luxembourg, R.drawable.flag_luxembourg),
    MACAU(R.string.macau_sar, R.drawable.flag_macao),
    MACEDONIA(R.string.macedonia, R.drawable.flag_macedonia),
    MADAGASCAR(R.string.madagascar, R.drawable.flag_madagascar),
    MALAWI(R.string.malawi, R.drawable.flag_malawi),
    MALAYSIA(R.string.malaysia, R.drawable.flag_malaysia),
    MALDIVES(R.string.maldives, R.drawable.flag_maldives),
    MALI(R.string.mali, R.drawable.flag_mali),
    MALTA(R.string.malta, R.drawable.flag_malta),
    ISLE_OF_MAN(R.string.man_isle_of, R.drawable.flag_isleof_man),
    MARSHALL_ISLANDS(R.string.marshall_islands, R.drawable.flag_marshall_islands),
    MARTINIQUE(R.string.martinique, R.drawable.flag_martinique),
    MAURITANIA(R.string.mauritania, R.drawable.flag_mauritania),
    MAURITIUS(R.string.mauritius, R.drawable.flag_mauritius),
    MAYOTTE(R.string.mayotte, R.drawable.flag_france),
    MEXICO(R.string.mexico, R.drawable.flag_mexico),
    MICRONESIA(R.string.micronesia, R.drawable.flag_micronesia),
    MOLDOVA(R.string.moldova, R.drawable.flag_moldova),
    MONACO(R.string.monaco, R.drawable.flag_monaco),
    MONGOLIA(R.string.mongolia, R.drawable.flag_mongolia),
    MONTSERRAT(R.string.montserrat, R.drawable.flag_montserrat),
    MOROCCO(R.string.morocco, R.drawable.flag_morocco),
    MOZAMBIQUE(R.string.mozambique, R.drawable.flag_mozambique),
    MYANMAR(R.string.myanmar, R.drawable.flag_myanmar),
    NAMIBIA(R.string.namibia, R.drawable.flag_namibia),
    NAURU(R.string.nauru, R.drawable.flag_nauru),
    NEPAL(R.string.nepal, R.drawable.flag_nepal),
    NETHERLANDS_ANTILLES(R.string.netherlands_antilles, R.drawable.flag_netherlands_antilles),
    NETHERLANDS(R.string.netherlands, R.drawable.flag_netherlands),
    NEW_CALEDONIA(R.string.new_caledonia, R.drawable.flag_new_caledonia),
    NEW_ZEALAND(R.string.new_zealand, R.drawable.flag_new_zealand),
    NICARAGUA(R.string.nicaragua, R.drawable.flag_nicaragua),
    NIGER(R.string.niger, R.drawable.flag_niger),
    NIGERIA(R.string.nigeria, R.drawable.flag_nigeria),
    NIUE(R.string.niue, R.drawable.flag_niue),
    NORFOLK_ISLAND(R.string.norfolk_island, R.drawable.flag_norfolk_island),
    NORTHERN_MARIANA_ISLANDS(R.string.northern_mariana_islands, R.drawable.flag_northern_mariana_islands),
    NORWAY(R.string.norway, R.drawable.flag_norway),
    OMAN(R.string.oman, R.drawable.flag_oman),
    PAKISTAN(R.string.pakistan, R.drawable.flag_pakistan),
    PALAU(R.string.palau, R.drawable.flag_palau),
    PALESTINE(R.string.palestinian_territory_occupied, R.drawable.flag_palestine),
    PANAMA(R.string.panama, R.drawable.flag_panama),
    PAPUA_NEW_GUINEA(R.string.papua_new_guinea, R.drawable.flag_papua_new_guinea),
    PARAGUAY(R.string.paraguay, R.drawable.flag_paraguay),
    PERU(R.string.peru, R.drawable.flag_peru),
    PHILIPPINES(R.string.philippines, R.drawable.flag_philippines),
    PITCAIRN_ISLAND(R.string.pitcairn_island, R.drawable.flag_pitcairn_islands),
    POLAND(R.string.poland, R.drawable.flag_poland),
    PORTUGAL(R.string.portugal, R.drawable.flag_portugal),
    PUERTO_RICO(R.string.puerto_rico, R.drawable.flag_puerto_rico),
    QATAR(R.string.qatar, R.drawable.flag_qatar),
    REUNION(R.string.reunion, R.drawable.flag_france),
    ROMANIA(R.string.romania, R.drawable.flag_romania),
    RUSSIA(R.string.russia, R.drawable.flag_russian_federation),
    RWANDA(R.string.rwanda, R.drawable.flag_rwanda),
    SAINT_HELENA(R.string.saint_helena, R.drawable.flag_saint_helena),
    SAINT_KITTS_AND_NEVIS(R.string.saint_kitts_and_nevis, R.drawable.flag_saint_kitts_and_nevis),
    SAINT_LUCIA(R.string.saint_lucia, R.drawable.flag_saint_lucia),
    SAINT_PIERRE_AND_MIQUELON(R.string.saint_pierre_and_miquelon, R.drawable.flag_saint_pierre),
    SAINT_VINCENT_AND_THE_GRENADINES(R.string.saint_vincent_and_the_grenadines, R.drawable.flag_saint_vicent_and_the_grenadines),
    SAMOA(R.string.samoa, R.drawable.flag_samoa),
    SAN_MARINO(R.string.san_marino, R.drawable.flag_san_marino),
    SAO_TOME_AND_PRINCIPE(R.string.sao_tome_and_principe, R.drawable.flag_sao_tome_and_principe),
    SAUDI_ARABIA(R.string.saudi_arabia, R.drawable.flag_saudi_arabia),
    SENEGAL(R.string.senegal, R.drawable.flag_senegal),
    SERBIA(R.string.serbia, R.drawable.flag_serbia),
    SEYCHELLES(R.string.seychelles, R.drawable.flag_seychelles),
    SIERRA_LEONE(R.string.sierra_leone, R.drawable.flag_sierra_leone),
    SINGAPORE(R.string.singapore, R.drawable.flag_singapore),
    SLOVAKIA(R.string.slovakia, R.drawable.flag_slovakia),
    SLOVENIA(R.string.slovenia, R.drawable.flag_slovenia),
    SMALLER_TERRITORIES_OF_THE_UK(R.string.smaller_territories_of_the_uk, R.drawable.flag_united_kingdom),
    SOLOMON_ISLANDS(R.string.solomon_islands, R.drawable.flag_soloman_islands),
    SOMALIA(R.string.somalia, R.drawable.flag_somalia),
    SOUTH_AFRICA(R.string.south_africa, R.drawable.flag_south_africa),
    SOUTH_GEORGIA(R.string.south_georgia, R.drawable.flag_south_georgia),
    SOUTH_SUDAN(R.string.south_sudan, R.drawable.flag_south_sudan),
    SPAIN(R.string.spain, R.drawable.flag_spain),
    SRI_LANKA(R.string.sri_lanka, R.drawable.flag_sri_lanka),
    SUDAN(R.string.sudan, R.drawable.flag_sudan),
    SURINAME(R.string.suriname, R.drawable.flag_suriname),
    SVALBARD_AND_JAN_MAYEN_ISLANDS(R.string.svalbard_and_jan_mayen_islands, R.drawable.flag_norway),
    SWAZILAND(R.string.swaziland, R.drawable.flag_swaziland),
    SWEDEN(R.string.sweden, R.drawable.flag_sweden),
    SWITZERLAND(R.string.switzerland, R.drawable.flag_switzerland),
    SYRIA(R.string.syria, R.drawable.flag_syria),
    TAIWAN(R.string.taiwan, R.drawable.flag_taiwan),
    TAJIKISTAN(R.string.tajikistan, R.drawable.flag_tajikistan),
    TANZANIA(R.string.tanzania, R.drawable.flag_tanzania),
    THAILAND(R.string.thailand, R.drawable.flag_thailand),
    TOGO(R.string.togo, R.drawable.flag_togo),
    TOKELAU(R.string.tokelau, R.drawable.flag_tokelau),
    TONGA(R.string.tonga, R.drawable.flag_tonga),
    TRINIDAD_AND_TOBAGO(R.string.trinidad_and_tobago, R.drawable.flag_trinidad_and_tobago),
    TUNISIA(R.string.tunisia, R.drawable.flag_tunisia),
    TURKEY(R.string.turkey, R.drawable.flag_turkey),
    TURKMENISTAN(R.string.turkmenistan, R.drawable.flag_turkmenistan),
    TURKS_AND_CAICOS_ISLANDS(R.string.turks_and_caicos_islands, R.drawable.flag_turks_and_caicos_islands),
    TUVALU(R.string.tuvalu, R.drawable.flag_tuvalu),
    UGANDA(R.string.uganda, R.drawable.flag_uganda),
    UKRAINE(R.string.ukraine, R.drawable.flag_ukraine),
    UNITED_ARAB_EMIRATES(R.string.united_arab_emirates, R.drawable.flag_emirates),
    UNITED_KINGDOM(R.string.united_kingdom, R.drawable.flag_united_kingdom),
    UNITED_STATES(R.string.united_states, R.drawable.flag_united_states_of_america),
    UNITED_STATES_MINOR_OUTLYING_ISLANDS(R.string.united_states_minor_outlying_islands, R.drawable.flag_united_states_of_america),
    URUGUAY(R.string.uruguay, R.drawable.flag_uruguay),
    UZBEKISTAN(R.string.uzbekistan, R.drawable.flag_uzbekistan),
    VANUATU(R.string.vanuatu, R.drawable.flag_vanuatu),
    VATICAN_CITY(R.string.vatican_city, R.drawable.flag_vatican_city),
    VENEZUELA(R.string.venezuela, R.drawable.flag_venezuela),
    VIETNAM(R.string.vietnam, R.drawable.flag_vietnam),
    VIRGIN_ISLANDS_BRITISH(R.string.virgin_islands_british, R.drawable.flag_british_virgin_islands),
    VIRGIN_ISLANDS_US(R.string.virgin_islands_us, R.drawable.flag_us_virgin_islands),
    WALLIS_AND_FUTUNA_ISLANDS(R.string.wallis_and_futuna_islands, R.drawable.flag_wallis_and_futuna),
    WESTERN_SAHARA(R.string.western_sahara, R.drawable.flag_sahara),
    YEMEN(R.string.yemen, R.drawable.flag_yemen),
    YUGOSLAVIA(R.string.yugoslavia, R.drawable.flag_yugoslavia),
    ZAMBIA(R.string.zambia, R.drawable.flag_zambia),
    ZIMBABWE(R.string.zimbabwe, R.drawable.flag_zimbabwe);

    fun getLabel(context: Context) = context.getString(this.label)
}