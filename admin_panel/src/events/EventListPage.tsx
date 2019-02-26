import * as React from "react";
import {Event} from "./Event"
import {format} from "date-fns";
import {connect} from "react-redux";
import {AppState} from "../store/AppState";
import {IDMap} from "../store/IDMap";
import {AppDispatch} from "../store/appStore";
import {loadEvents} from "../store/actions/events/LoadEvents";
import {Container} from "../store/Container";
import {Link, RouteComponentProps} from "react-router-dom";


// props to be passed in by mapStateToProps
interface ReduxStateProps {
    events: Event[],
    allEvents: Container<{}> // state of loading all events
}

// props to be passed in by mapDispatchToProps
interface ReduxDispatchProps {
    loadEvents: () => void,
}

// combined props type
type Props = RouteComponentProps<{}> & ReduxStateProps & ReduxDispatchProps;

// unconnected component
class UnconnectedEventListPage extends React.Component<Props, {}> {


    public constructor(props: Readonly<Props>) {
        super(props);

    }

    public componentDidMount(): void {
        if (Container.isEmpty(this.props.allEvents)) {
            this.props.loadEvents();
        }
    }

    public render(): React.ReactNode {
        const eventComponents = [];


        for (const event of this.props.events) {
            eventComponents.push(<EventDisplay key={event.id} event={event}/>);
        }

        return <>
            <h1>Events</h1>
            <button type="button" className="btn btn-primary" onClick={this.newEvent}>New Event</button>
            <br/>
            <table className="table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Start Time</th>
                    <th>End Time</th>
                    <th>Location</th>
                </tr>
                </thead>
                <tbody>
                {eventComponents}
                </tbody>
            </table>
        </>;
    }

    private newEvent = () => {
        this.props.history.push("/event/new")
    };
}

function mapStateToProps(state: AppState): ReduxStateProps {
    return {
        events: IDMap.values(state.events).filter(Container.isReady).map(e => e.data).sort(Event.sortStartTime),
        allEvents: state.allEvents,
    };
}

function mapDispatchToProps(dispatch: AppDispatch): ReduxDispatchProps {
    return {
        loadEvents: () => dispatch(loadEvents()),
    };
}

export const EventListPage = connect(mapStateToProps, mapDispatchToProps)(UnconnectedEventListPage);

// event display item
interface EventDisplayProps {
    event: Event
}

class EventDisplay extends React.Component<EventDisplayProps, {}> {

    public render(): React.ReactNode {
        return <tr>
            <td><Link to={"/event/" + this.props.event.id}>{this.props.event.name}</Link></td>
            <td>{this.props.event.description}</td>
            <td>{format(this.props.event.startTime, "MMM d HH:mm")}</td>
            <td>{format(this.props.event.endTime, "MMM d HH:mm")}</td>
            <td>{this.props.event.location || "None"}</td>
        </tr>;
    }
}