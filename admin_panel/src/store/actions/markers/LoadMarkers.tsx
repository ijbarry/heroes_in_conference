import {AppThunkAction} from "../../AppActions";
import {API} from "../../../api/API";
import {updateMarkerCache} from "./UpdateMarkerCache";
import {Container, ErrorState} from "../../Container";
import {IDMap} from "../../IDMap";


export function loadMarkers(): AppThunkAction {
    return dispatch => {

        // we have started loading
        dispatch(updateMarkerCache(Container.loading(Date.now())));

        API.getMapMarkers()
            .then(markers => {
                // use the same time for all containers
                const currentTime = Date.now();

                // loaded containers
                const containers = markers.map(marker => Container.synced(marker, currentTime));

                // IDMap
                const idMapOfMarkers = IDMap.fromArray(containers, marker => marker.data.id);

                // MutableCache is really just Container<IDMap<LoadedContainer<T>>>
                dispatch(updateMarkerCache(Container.synced(idMapOfMarkers, currentTime)));
            })
            .catch(reason => {
                const error: ErrorState = {
                    timeErrored: Date.now(),
                    tries: 1,
                    errorMsg: reason,
                    errorData: reason,
                };

                dispatch(updateMarkerCache(Container.errored(error)));
            });
    }
}