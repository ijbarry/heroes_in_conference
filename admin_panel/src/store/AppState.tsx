import {Event} from "../events/Event";
import {IDMap} from "./IDMap";
import {Container} from "./Container";



export interface AppState {
    events: IDMap<Container<Event>>,
    allEvents: Container<{}>, // simply for tracking whether we have requested to load all events
}