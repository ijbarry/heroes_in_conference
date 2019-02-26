import {Container} from "../Container";
import {ConferenceMap} from "../../maps/ConferenceMap";
import {AppActionTypes, AppObjectAction} from "../AppActions";
import {Cache} from "../Cache";

export function reduceMapCache(state: Cache<ConferenceMap> | undefined, action: AppObjectAction): Cache<ConferenceMap> {
    switch(action.type){
        case AppActionTypes.UPDATE_MAP_CACHE: {
            // swap out the map cache for the new data
            return action.cache;
        }
        case AppActionTypes.UPDATE_CACHED_MAP: {
            // Update Cached Map shouldn't arrive unless we have the maps from the server
            return state ? Cache.updateItem(state, action.mapId, action.map) : Container.empty();
        }
        default: {
            return state || Container.empty();
        }
    }
}