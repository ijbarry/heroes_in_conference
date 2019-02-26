import {API} from "../../../api/API";
import {AppDispatch} from "../../appStore";
import {ConferenceMap} from "../../../maps/ConferenceMap";
import {updateCachedMap} from "./UpdateCachedMap";


/**
 * Deletes a map server side. If successful also removes the event from the cache.
 */
export function deleteMap(map: ConferenceMap, dispatch: AppDispatch): Promise<void> {
    return API.deleteMap(map.id).then(value => {

        // TODO also remove the markers of this map?
        dispatch(updateCachedMap(null, map.id));
    });
}