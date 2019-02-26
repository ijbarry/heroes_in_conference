import {AppActionTypes} from "../../AppActions";
import {Action} from "redux";
import {ConferenceMap} from "../../../maps/ConferenceMap";


export interface UpdateCachedMapAction extends Action<AppActionTypes> {
    type: AppActionTypes.UPDATE_CACHED_MAP,
    mapId: string,
    map: ConferenceMap | null, // null for deleted
}

export function updateCachedMap(map: ConferenceMap | null, mapId?: string): UpdateCachedMapAction {
    let id = mapId;

    if(!id && map) {
        id = map.id;
    }

    if(!id) {
        throw new Error("No map id provided to updateCachedMap");
    }

    return {
        type: AppActionTypes.UPDATE_CACHED_MAP,
        mapId: id,
        map,
    };
}