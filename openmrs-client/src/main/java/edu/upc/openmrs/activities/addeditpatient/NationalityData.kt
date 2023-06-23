package edu.upc.openmrs.activities.addeditpatient

import android.content.Context
import edu.upc.R

class NationalityData {
    companion object {
        fun getNationalities(context: Context): Array<Nationality> {
            return arrayOf(
                Nationality(context.getString(R.string.nationality_default), R.drawable.flag_blank),
                Nationality(context.getString(R.string.afghanistan), R.drawable.flag_afghanistan),
                Nationality(context.getString(R.string.albania), R.drawable.flag_albania),
                Nationality(context.getString(R.string.algeria), R.drawable.flag_algeria),
                Nationality(context.getString(R.string.american_samoa), R.drawable.flag_american_samoa),
                Nationality(context.getString(R.string.andorra), R.drawable.flag_andorra),
                Nationality(context.getString(R.string.angola), R.drawable.flag_angola),
                Nationality(context.getString(R.string.antarctica), R.drawable.flag_antarctica),
                Nationality(context.getString(R.string.antigua_and_barbuda), R.drawable.flag_antigua_and_barbuda),
                Nationality(context.getString(R.string.argentina), R.drawable.flag_argentina),
                Nationality(context.getString(R.string.aruba), R.drawable.flag_aruba),
                Nationality(context.getString(R.string.australia), R.drawable.flag_australia),
                Nationality(context.getString(R.string.austria), R.drawable.flag_austria),
                Nationality(context.getString(R.string.azerbaijan), R.drawable.flag_azerbaijan),
                Nationality(context.getString(R.string.bahamas), R.drawable.flag_bahamas),
                Nationality(context.getString(R.string.bahrain), R.drawable.flag_bahrain),
                Nationality(context.getString(R.string.bangladesh), R.drawable.flag_bangladesh),
                Nationality(context.getString(R.string.barbados), R.drawable.flag_barbados),
                Nationality(context.getString(R.string.belarus), R.drawable.flag_belarus),
                Nationality(context.getString(R.string.belgium), R.drawable.flag_belgium),
                Nationality(context.getString(R.string.belize), R.drawable.flag_belize),
                Nationality(context.getString(R.string.benin), R.drawable.flag_benin),
                Nationality(context.getString(R.string.bermuda), R.drawable.flag_bermuda),
                Nationality(context.getString(R.string.bhutan), R.drawable.flag_bhutan),
                Nationality(context.getString(R.string.bolivia), R.drawable.flag_bolivia),
                Nationality(context.getString(R.string.bosnia_and_herzegovina), R.drawable.flag_bosnia),
                Nationality(context.getString(R.string.botswana), R.drawable.flag_botswana),
                Nationality(context.getString(R.string.bouvet_island), R.drawable.flag_norway),
                Nationality(context.getString(R.string.brazil), R.drawable.flag_brazil),
                Nationality(context.getString(R.string.british_indian_ocean_territory), R.drawable.flag_british_indian_ocean_territory),
                Nationality(context.getString(R.string.brunei), R.drawable.flag_brunei),
                Nationality(context.getString(R.string.bulgaria), R.drawable.flag_bulgaria),
                Nationality(context.getString(R.string.burkina_faso), R.drawable.flag_burkina_faso),
                Nationality(context.getString(R.string.burundi), R.drawable.flag_burundi),
                Nationality(context.getString(R.string.cambodia), R.drawable.flag_cambodia),
                Nationality(context.getString(R.string.cameroon), R.drawable.flag_cameroon),
                Nationality(context.getString(R.string.canada), R.drawable.flag_canada),
                Nationality(context.getString(R.string.cape_verde), R.drawable.flag_cape_verde),
                Nationality(context.getString(R.string.cayman_islands), R.drawable.flag_cayman_islands),
                Nationality(context.getString(R.string.central_african_republic), R.drawable.flag_central_african_republic),
                Nationality(context.getString(R.string.chad), R.drawable.flag_chad),
                Nationality(context.getString(R.string.chile), R.drawable.flag_chile),
                Nationality(context.getString(R.string.china), R.drawable.flag_china),
                Nationality(context.getString(R.string.christmas_island), R.drawable.flag_christmas_island),
                Nationality(context.getString(R.string.cocos_islands), R.drawable.flag_cocos),
                Nationality(context.getString(R.string.colombia), R.drawable.flag_colombia),
                Nationality(context.getString(R.string.comoros), R.drawable.flag_comoros),
                Nationality(context.getString(R.string.congo), R.drawable.flag_republic_of_the_congo),
                Nationality(context.getString(R.string.congo_democratic_republic), R.drawable.flag_democratic_republic_of_the_congo),
                Nationality(context.getString(R.string.cook_islands), R.drawable.flag_cook_islands),
                Nationality(context.getString(R.string.costa_rica), R.drawable.flag_costa_rica),
                Nationality(context.getString(R.string.cote_divoire), R.drawable.flag_cote_divoire),
                Nationality(context.getString(R.string.croatia), R.drawable.flag_croatia),
                Nationality(context.getString(R.string.cuba), R.drawable.flag_cuba),
                Nationality(context.getString(R.string.cyprus), R.drawable.flag_cyprus),
                Nationality(context.getString(R.string.czech_republic), R.drawable.flag_czech_republic),
                Nationality(context.getString(R.string.denmark), R.drawable.flag_denmark),
                Nationality(context.getString(R.string.djibouti), R.drawable.flag_djibouti),
                Nationality(context.getString(R.string.dominica), R.drawable.flag_dominica),
                Nationality(context.getString(R.string.dominican_republic), R.drawable.flag_dominican_republic),
                Nationality(context.getString(R.string.east_timor), R.drawable.flag_timor_leste),
                Nationality(context.getString(R.string.ecuador), R.drawable.flag_ecuador),
                Nationality(context.getString(R.string.egypt), R.drawable.flag_egypt),
                Nationality(context.getString(R.string.el_salvador), R.drawable.flag_el_salvador),
                Nationality(context.getString(R.string.equatorial_guinea), R.drawable.flag_equatorial_guinea),
                Nationality(context.getString(R.string.eritrea), R.drawable.flag_eritrea),
                Nationality(context.getString(R.string.estonia), R.drawable.flag_estonia),
                Nationality(context.getString(R.string.ethiopia), R.drawable.flag_ethiopia),
                Nationality(context.getString(R.string.external_territories_australia), R.drawable.flag_australia),
                Nationality(context.getString(R.string.falkland_islands), R.drawable.flag_falkland_islands),
                Nationality(context.getString(R.string.faroe_islands), R.drawable.flag_faroe_islands),
                Nationality(context.getString(R.string.fiji), R.drawable.flag_fiji),
                Nationality(context.getString(R.string.finland), R.drawable.flag_finland),
                Nationality(context.getString(R.string.france), R.drawable.flag_france),
                Nationality(context.getString(R.string.french_guiana), R.drawable.flag_france),
                Nationality(context.getString(R.string.french_polynesia), R.drawable.flag_french_polynesia),
                Nationality(context.getString(R.string.french_southern_territories), R.drawable.flag_france),
                Nationality(context.getString(R.string.gabon), R.drawable.flag_gabon),
                Nationality(context.getString(R.string.gambia), R.drawable.flag_gambia),
                Nationality(context.getString(R.string.georgia), R.drawable.flag_georgia),
                Nationality(context.getString(R.string.germany), R.drawable.flag_germany),
                Nationality(context.getString(R.string.ghana), R.drawable.flag_ghana),
                Nationality(context.getString(R.string.gibraltar), R.drawable.flag_gibraltar),
                Nationality(context.getString(R.string.greece), R.drawable.flag_greece),
                Nationality(context.getString(R.string.greenland), R.drawable.flag_greenland),
                Nationality(context.getString(R.string.grenada), R.drawable.flag_grenada),
                Nationality(context.getString(R.string.guadeloupe), R.drawable.flag_guadeloupe),
                Nationality(context.getString(R.string.guam), R.drawable.flag_guam),
                Nationality(context.getString(R.string.guatemala), R.drawable.flag_guatemala),
                Nationality(context.getString(R.string.guernsey_and_alderney), R.drawable.flag_guernsey),
                Nationality(context.getString(R.string.guinea), R.drawable.flag_guinea),
                Nationality(context.getString(R.string.guinea_bissau), R.drawable.flag_guinea_bissau),
                Nationality(context.getString(R.string.guyana), R.drawable.flag_guyana),
                Nationality(context.getString(R.string.haiti), R.drawable.flag_haiti),
                Nationality(context.getString(R.string.heard_and_mcdonald_islands), R.drawable.flag_australia),
                Nationality(context.getString(R.string.honduras), R.drawable.flag_honduras),
                Nationality(context.getString(R.string.hong_kong_sar), R.drawable.flag_hong_kong),
                Nationality(context.getString(R.string.hungary), R.drawable.flag_hungary),
                Nationality(context.getString(R.string.iceland), R.drawable.flag_iceland),
                Nationality(context.getString(R.string.india), R.drawable.flag_india),
                Nationality(context.getString(R.string.indonesia), R.drawable.flag_indonesia),
                Nationality(context.getString(R.string.iran), R.drawable.flag_iran),
                Nationality(context.getString(R.string.iraq), R.drawable.flag_iraq),
                Nationality(context.getString(R.string.ireland), R.drawable.flag_ireland),
                Nationality(context.getString(R.string.israel), R.drawable.flag_israel),
                Nationality(context.getString(R.string.italy), R.drawable.flag_italy),
                Nationality(context.getString(R.string.jamaica), R.drawable.flag_jamaica),
                Nationality(context.getString(R.string.japan), R.drawable.flag_japan),
                Nationality(context.getString(R.string.jersey), R.drawable.flag_jersey),
                Nationality(context.getString(R.string.jordan), R.drawable.flag_jordan),
                Nationality(context.getString(R.string.kazakhstan), R.drawable.flag_kazakhstan),
                Nationality(context.getString(R.string.kenya), R.drawable.flag_kenya),
                Nationality(context.getString(R.string.kiribati), R.drawable.flag_kiribati),
                Nationality(context.getString(R.string.korea_north), R.drawable.flag_north_korea),
                Nationality(context.getString(R.string.korea_south), R.drawable.flag_south_korea),
                Nationality(context.getString(R.string.kuwait), R.drawable.flag_kuwait),
                Nationality(context.getString(R.string.kyrgyzstan), R.drawable.flag_kyrgyzstan),
                Nationality(context.getString(R.string.laos), R.drawable.flag_laos),
                Nationality(context.getString(R.string.latvia), R.drawable.flag_latvia),
                Nationality(context.getString(R.string.lebanon), R.drawable.flag_lebanon),
                Nationality(context.getString(R.string.lesotho), R.drawable.flag_lesotho),
                Nationality(context.getString(R.string.liberia), R.drawable.flag_liberia),
                Nationality(context.getString(R.string.libya), R.drawable.flag_libya),
                Nationality(context.getString(R.string.liechtenstein), R.drawable.flag_liechtenstein),
                Nationality(context.getString(R.string.lithuania), R.drawable.flag_lithuania),
                Nationality(context.getString(R.string.luxembourg), R.drawable.flag_luxembourg),
                Nationality(context.getString(R.string.macau_sar), R.drawable.flag_macao),
                Nationality(context.getString(R.string.macedonia), R.drawable.flag_macedonia),
                Nationality(context.getString(R.string.madagascar), R.drawable.flag_madagascar),
                Nationality(context.getString(R.string.malawi), R.drawable.flag_malawi),
                Nationality(context.getString(R.string.malaysia), R.drawable.flag_malaysia),
                Nationality(context.getString(R.string.maldives), R.drawable.flag_maldives),
                Nationality(context.getString(R.string.mali), R.drawable.flag_mali),
                Nationality(context.getString(R.string.malta), R.drawable.flag_malta),
                Nationality(context.getString(R.string.man_isle_of), R.drawable.flag_isleof_man),
                Nationality(context.getString(R.string.marshall_islands), R.drawable.flag_marshall_islands),
                Nationality(context.getString(R.string.martinique), R.drawable.flag_martinique),
                Nationality(context.getString(R.string.mauritania), R.drawable.flag_mauritania),
                Nationality(context.getString(R.string.mauritius), R.drawable.flag_mauritius),
                Nationality(context.getString(R.string.mayotte), R.drawable.flag_france),
                Nationality(context.getString(R.string.mexico), R.drawable.flag_mexico),
                Nationality(context.getString(R.string.micronesia), R.drawable.flag_micronesia),
                Nationality(context.getString(R.string.moldova), R.drawable.flag_moldova),
                Nationality(context.getString(R.string.monaco), R.drawable.flag_monaco),
                Nationality(context.getString(R.string.mongolia), R.drawable.flag_mongolia),
                Nationality(context.getString(R.string.montserrat), R.drawable.flag_montserrat),
                Nationality(context.getString(R.string.morocco), R.drawable.flag_morocco),
                Nationality(context.getString(R.string.mozambique), R.drawable.flag_mozambique),
                Nationality(context.getString(R.string.myanmar), R.drawable.flag_myanmar),
                Nationality(context.getString(R.string.namibia), R.drawable.flag_namibia),
                Nationality(context.getString(R.string.nauru), R.drawable.flag_nauru),
                Nationality(context.getString(R.string.nepal), R.drawable.flag_nepal),
                Nationality(context.getString(R.string.netherlands_antilles), R.drawable.flag_netherlands_antilles),
                Nationality(context.getString(R.string.netherlands), R.drawable.flag_netherlands),
                Nationality(context.getString(R.string.new_caledonia), R.drawable.flag_new_caledonia),
                Nationality(context.getString(R.string.new_zealand), R.drawable.flag_new_zealand),
                Nationality(context.getString(R.string.nicaragua), R.drawable.flag_nicaragua),
                Nationality(context.getString(R.string.niger), R.drawable.flag_niger),
                Nationality(context.getString(R.string.nigeria), R.drawable.flag_nigeria),
                Nationality(context.getString(R.string.niue), R.drawable.flag_niue),
                Nationality(context.getString(R.string.norfolk_island), R.drawable.flag_norfolk_island),
                Nationality(context.getString(R.string.northern_mariana_islands), R.drawable.flag_northern_mariana_islands),
                Nationality(context.getString(R.string.norway), R.drawable.flag_norway),
                Nationality(context.getString(R.string.oman), R.drawable.flag_oman),
                Nationality(context.getString(R.string.pakistan), R.drawable.flag_pakistan),
                Nationality(context.getString(R.string.palau), R.drawable.flag_palau),
                Nationality(context.getString(R.string.palestinian_territory_occupied), R.drawable.flag_palestine),
                Nationality(context.getString(R.string.panama), R.drawable.flag_panama),
                Nationality(context.getString(R.string.papua_new_guinea), R.drawable.flag_papua_new_guinea),
                Nationality(context.getString(R.string.paraguay), R.drawable.flag_paraguay),
                Nationality(context.getString(R.string.peru), R.drawable.flag_peru),
                Nationality(context.getString(R.string.philippines), R.drawable.flag_philippines),
                Nationality(context.getString(R.string.pitcairn_island), R.drawable.flag_pitcairn_islands),
                Nationality(context.getString(R.string.poland), R.drawable.flag_poland),
                Nationality(context.getString(R.string.portugal), R.drawable.flag_portugal),
                Nationality(context.getString(R.string.puerto_rico), R.drawable.flag_puerto_rico),
                Nationality(context.getString(R.string.qatar), R.drawable.flag_qatar),
                Nationality(context.getString(R.string.reunion), R.drawable.flag_france),
                Nationality(context.getString(R.string.romania), R.drawable.flag_romania),
                Nationality(context.getString(R.string.russia), R.drawable.flag_russian_federation),
                Nationality(context.getString(R.string.rwanda), R.drawable.flag_rwanda),
                Nationality(context.getString(R.string.saint_helena), R.drawable.flag_saint_helena),
                Nationality(context.getString(R.string.saint_kitts_and_nevis), R.drawable.flag_saint_kitts_and_nevis),
                Nationality(context.getString(R.string.saint_lucia), R.drawable.flag_saint_lucia),
                Nationality(context.getString(R.string.saint_pierre_and_miquelon), R.drawable.flag_saint_pierre),
                Nationality(context.getString(R.string.saint_vincent_and_the_grenadines), R.drawable.flag_saint_vicent_and_the_grenadines),
                Nationality(context.getString(R.string.samoa), R.drawable.flag_samoa),
                Nationality(context.getString(R.string.san_marino), R.drawable.flag_san_marino),
                Nationality(context.getString(R.string.sao_tome_and_principe), R.drawable.flag_sao_tome_and_principe),
                Nationality(context.getString(R.string.saudi_arabia), R.drawable.flag_saudi_arabia),
                Nationality(context.getString(R.string.senegal), R.drawable.flag_senegal),
                Nationality(context.getString(R.string.serbia), R.drawable.flag_serbia),
                Nationality(context.getString(R.string.seychelles), R.drawable.flag_seychelles),
                Nationality(context.getString(R.string.sierra_leone), R.drawable.flag_sierra_leone),
                Nationality(context.getString(R.string.singapore), R.drawable.flag_singapore),
                Nationality(context.getString(R.string.slovakia), R.drawable.flag_slovakia),
                Nationality(context.getString(R.string.slovenia), R.drawable.flag_slovenia),
                Nationality(context.getString(R.string.smaller_territories_of_the_uk), R.drawable.flag_united_kingdom),
                Nationality(context.getString(R.string.solomon_islands), R.drawable.flag_soloman_islands),
                Nationality(context.getString(R.string.somalia), R.drawable.flag_somalia),
                Nationality(context.getString(R.string.south_africa), R.drawable.flag_south_africa),
                Nationality(context.getString(R.string.south_georgia), R.drawable.flag_south_georgia),
                Nationality(context.getString(R.string.south_sudan), R.drawable.flag_south_sudan),
                Nationality(context.getString(R.string.spain), R.drawable.flag_spain),
                Nationality(context.getString(R.string.sri_lanka), R.drawable.flag_sri_lanka),
                Nationality(context.getString(R.string.sudan), R.drawable.flag_sudan),
                Nationality(context.getString(R.string.suriname), R.drawable.flag_suriname),
                Nationality(context.getString(R.string.svalbard_and_jan_mayen_islands), R.drawable.flag_norway),
                Nationality(context.getString(R.string.swaziland), R.drawable.flag_swaziland),
                Nationality(context.getString(R.string.sweden), R.drawable.flag_sweden),
                Nationality(context.getString(R.string.switzerland), R.drawable.flag_switzerland),
                Nationality(context.getString(R.string.syria), R.drawable.flag_syria),
                Nationality(context.getString(R.string.taiwan), R.drawable.flag_taiwan),
                Nationality(context.getString(R.string.tajikistan), R.drawable.flag_tajikistan),
                Nationality(context.getString(R.string.tanzania), R.drawable.flag_tanzania),
                Nationality(context.getString(R.string.thailand), R.drawable.flag_thailand),
                Nationality(context.getString(R.string.togo), R.drawable.flag_togo),
                Nationality(context.getString(R.string.tokelau), R.drawable.flag_tokelau),
                Nationality(context.getString(R.string.tonga), R.drawable.flag_tonga),
                Nationality(context.getString(R.string.trinidad_and_tobago), R.drawable.flag_trinidad_and_tobago),
                Nationality(context.getString(R.string.tunisia), R.drawable.flag_tunisia),
                Nationality(context.getString(R.string.turkey), R.drawable.flag_turkey),
                Nationality(context.getString(R.string.turkmenistan), R.drawable.flag_turkmenistan),
                Nationality(context.getString(R.string.turks_and_caicos_islands), R.drawable.flag_turks_and_caicos_islands),
                Nationality(context.getString(R.string.tuvalu), R.drawable.flag_tuvalu),
                Nationality(context.getString(R.string.uganda), R.drawable.flag_uganda),
                Nationality(context.getString(R.string.ukraine), R.drawable.flag_ukraine),
                Nationality(context.getString(R.string.united_arab_emirates), R.drawable.flag_emirates),
                Nationality(context.getString(R.string.united_kingdom), R.drawable.flag_united_kingdom),
                Nationality(context.getString(R.string.united_states), R.drawable.flag_united_states_of_america),
                Nationality(context.getString(R.string.united_states_minor_outlying_islands), R.drawable.flag_united_states_of_america),
                Nationality(context.getString(R.string.uruguay), R.drawable.flag_uruguay),
                Nationality(context.getString(R.string.uzbekistan), R.drawable.flag_uzbekistan),
                Nationality(context.getString(R.string.vanuatu), R.drawable.flag_vanuatu),
                Nationality(context.getString(R.string.vatican_city), R.drawable.flag_vatican_city),
                Nationality(context.getString(R.string.venezuela), R.drawable.flag_venezuela),
                Nationality(context.getString(R.string.vietnam), R.drawable.flag_vietnam),
                Nationality(context.getString(R.string.virgin_islands_british), R.drawable.flag_british_virgin_islands),
                Nationality(context.getString(R.string.virgin_islands_us), R.drawable.flag_us_virgin_islands),
                Nationality(context.getString(R.string.wallis_and_futuna_islands), R.drawable.flag_wallis_and_futuna),
                Nationality(context.getString(R.string.western_sahara), R.drawable.flag_sahara),
                Nationality(context.getString(R.string.yemen), R.drawable.flag_yemen),
                Nationality(context.getString(R.string.yugoslavia), R.drawable.flag_yugoslavia),
                Nationality(context.getString(R.string.zambia), R.drawable.flag_zambia),
                Nationality(context.getString(R.string.zimbabwe), R.drawable.flag_zimbabwe)
                )
        }
    }
}