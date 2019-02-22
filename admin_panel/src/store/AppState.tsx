import {Event} from "../events/Event";
import {IDMap} from "./IDMap";
import {Container} from "./Container";
import {ConferenceMap} from "../maps/ConferenceMap";
import {Cache, MutableCache} from "./Cache";
import {MapMarker} from "../maps/MapMarker";

export interface AppState {
    events: IDMap<Container<Event>>,
    allEvents: Container<{}>, // simply for tracking whether we have requested to load all events
    mapCache: Cache<ConferenceMap>, // let's try this way of doing it instead
    markerCache: MutableCache<MapMarker>,
}