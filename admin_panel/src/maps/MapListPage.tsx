import * as React from "react";
import {ConferenceMap} from "./ConferenceMap";
import {Link, RouteComponentProps} from "react-router-dom";
import {IDMap} from "../store/IDMap";
import {Container} from "../store/Container";
import {connect} from "react-redux";
import {AppState} from "../store/AppState";
import {AppDispatch} from "../store/appStore";
import {loadMaps} from "../store/actions/maps/LoadMaps";
import {Cache} from "../store/Cache";

interface ReduxStateProps {
    maps: Cache<ConferenceMap>,
}

interface ReduxDispatchProps {
    loadMaps: () => void,
}

type Props = RouteComponentProps<{}> & ReduxStateProps & ReduxDispatchProps;

class UnconnectedMapListPage extends React.Component<Props, {}> {

    public componentDidMount(): void {
        if(Container.isEmpty(this.props.maps)) {
            this.props.loadMaps();
        }
    }

    public render(): React.ReactNode {
        let mapComponents = null;

        if(Container.isReady(this.props.maps)){
            const maps = IDMap.values(this.props.maps.data).sort(ConferenceMap.sortByName);
            mapComponents = maps.map(map => <MapListItem key={map.id} map={map}/>);
        }

        return <>
            <h1>Maps</h1>
            <button type="button" className="btn btn-primary" onClick={this.newMap}>New Map</button>
            <br/>
            <table className="table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Path</th>
                </tr>
                </thead>
                <tbody>
                {mapComponents}
                </tbody>
            </table>
        </>;
    }

    private newMap = () => {
        this.props.history.push("/map/new");
    };
}

function mapStateToProps(state: AppState): ReduxStateProps {
    return {
        maps: state.mapCache,
    };
}

function mapDispatchToProps(dispatch: AppDispatch): ReduxDispatchProps {
    return {
        loadMaps: () => dispatch(loadMaps()),
    };
}

export const MapListPage = connect(mapStateToProps, mapDispatchToProps)(UnconnectedMapListPage);

interface MapListItemProps {
    map: ConferenceMap,
}

class MapListItem extends React.Component<MapListItemProps, {}> {

    public render(): React.ReactNode {
        return <tr>
            <td><Link to={"/map/" + this.props.map.id}>{this.props.map.name}</Link></td>
            <td>{this.props.map.path}</td>
        </tr>;
    }
}