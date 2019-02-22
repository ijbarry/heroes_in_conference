import {Action} from "redux";
import {AppActionTypes} from "../../AppActions";
import {IDMap} from "../../IDMap";
import {ConferenceMap} from "../../../maps/ConferenceMap";
import {Container} from "../../Container";


export interface UpdateMapCacheAction extends Action<AppActionTypes> {
    type: AppActionTypes.UPDATE_MAP_CACHE,
    cache: Container<IDMap<ConferenceMap>>,
}

export function updateMapCache(cache: Container<IDMap<ConferenceMap>>): UpdateMapCacheAction {
    return {
        type: AppActionTypes.UPDATE_MAP_CACHE,
        cache,
    };
}