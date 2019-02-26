import {AppThunkAction} from "../../AppActions";
import {API} from "../../../api/API";
import {updateMapCache} from "./UpdateMapCache";
import {Container, ErrorState} from "../../Container";
import {IDMap} from "../../IDMap";


export function loadMaps(): AppThunkAction {
    return dispatch => {

        // we have started loading
        dispatch(updateMapCache(Container.loading(Date.now())));

        API.getMaps()
            .then(maps => {
                const idMapOfMaps = IDMap.fromArray(maps, map => map.id);
                dispatch(updateMapCache(Container.synced(idMapOfMaps, Date.now())));
            })
            .catch(reason => {
                const error: ErrorState = {
                    timeErrored: Date.now(),
                    tries: 1,
                    errorMsg: reason,
                    errorData: reason,
                };

                dispatch(updateMapCache(Container.errored(error)));
            });
    }
}