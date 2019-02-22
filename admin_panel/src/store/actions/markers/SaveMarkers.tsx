import {AppThunkAction} from "../../AppActions";
import {Container, DeletedContainer, ErrorState} from "../../Container";
import {IDMap} from "../../IDMap";
import {MapMarker} from "../../../maps/MapMarker";
import {API} from "../../../api/API";
import {updateCachedMarkers} from "./UpdateCachedMarkers";

export function saveMarkers(): AppThunkAction {
    return (dispatch, getState) => {
        const cache = getState().markerCache;

        if (!Container.isReady(cache)) {
            // can't save if we haven't reloaded
            return;
        }

        const items = cache.data;

        const modifiedItems: MapMarker[] = [];
        const deletedItems: string[] = [];

        IDMap.iterate(items, (item, id) => {
            if (Container.isModified(item)) {
                modifiedItems.push(item.data);
            } else if (Container.isDeleted(item)) {
                deletedItems.push(id);
            }
        });

        API.updateMapMarkers(modifiedItems, deletedItems)
            .then(() => {
                const now = Date.now();
                const syncedItemsList = modifiedItems.map(value => Container.synced(value, now));
                const modifiedMap = IDMap.fromArray(syncedItemsList, item => item.data.id);

                const deletedMap: IDMap<null> = {};
                for (const deletedItem of deletedItems) {
                    deletedMap[deletedItem] = null;
                }

                // update the markers in the cache
                dispatch(updateCachedMarkers({...modifiedMap, ...deletedMap}));
            })
            .catch(reason => {
                const now = Date.now();
                const error: ErrorState = {
                    timeErrored: now,
                    tries: 1,
                    errorMsg: reason,
                    errorData: reason,
                };

                const modifiedItemsList = modifiedItems.map(value => Container.modified(value, now, error));
                const modifiedMap = IDMap.fromArray(modifiedItemsList, item => item.data.id);

                const deletedMap: IDMap<DeletedContainer> = {};
                for(const deletedItem of deletedItems) {
                    deletedMap[deletedItem] = Container.deleted(now, error);
                }

                // update the markers with the errors
                dispatch(updateCachedMarkers({...modifiedMap, ...deletedMap}));
            });
    };
}