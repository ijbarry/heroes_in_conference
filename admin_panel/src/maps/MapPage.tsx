import * as React from "react";
import {ChangeEvent} from "react";
import {ConferenceMap} from "./ConferenceMap";
import {connect} from "react-redux";
import {AppState} from "../store/AppState";
import {RouteComponentProps} from "react-router";
import {Cache, CacheItem, CacheItemState, MutableCache} from "../store/Cache";
import * as L from "leaflet";
import {LatLngBoundsLiteral, Map, Marker} from "leaflet";
// no types for react-leaflet
// @ts-ignore
import * as RL from 'react-leaflet';
import {GridPos, MapMarker} from "./MapMarker";
import {AppDispatch} from "../store/appStore";
import {loadMaps} from "../store/actions/maps/LoadMaps";
import {loadMarkers} from "../store/actions/markers/LoadMarkers";
import {Container} from "../store/Container";
import {IDMap} from "../store/IDMap";
import {updateMap} from "../store/actions/maps/UpdateMap";
import {deleteMap} from "../store/actions/maps/DeleteMap";
import {updateCachedMarkers} from "../store/actions/markers/UpdateCachedMarkers";
import {saveMarkers} from "../store/actions/markers/SaveMarkers";

const ReactLeaflet = RL as any;

interface RouteParams {
    id: string,
}

interface ReduxStateProps {
    map: CacheItem<ConferenceMap>,
    markers: MutableCache<MapMarker>,
}

interface ReduxDispatchProps {
    loadMaps: () => void,
    loadMarkers: () => void,
    updateMap: (map: ConferenceMap, imageUrl?: string) => Promise<ConferenceMap>,
    deleteMap: (map: ConferenceMap) => Promise<void>,
    updateMarker: (marker: MapMarker) => void,
    deleteMarker: (marker: MapMarker) => void,
    saveMarkers: () => void,
}

type ConnectedProps = RouteComponentProps<RouteParams>;

type Props = ConnectedProps & ReduxStateProps & ReduxDispatchProps;

interface State {
    map?: ConferenceMap,
    statusMessage?: string,
    revokeURL?: string,
    bounds?: LatLngBoundsLiteral
}

class UnconnectedMapPage extends React.Component<Props, State> {
    private mapRef = React.createRef<any>();

    constructor(props: Props, context: any) {
        super(props, context);

        // initial state
        this.state = {};
    }

    public componentDidMount(): void {

        // ensure the map is loaded
        switch (this.props.map.state) {
            case CacheItemState.CACHE_UNLOADED:
            case CacheItemState.CACHE_ERROR:
                this.props.loadMaps();
        }

        // ensure the markers are loaded
        if (Container.isEmpty(this.props.markers) || Container.isErrored(this.props.markers)) {
            this.props.loadMarkers();
        }

        this.onUpdate();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.onUpdate();
    }

    public componentWillUnmount(): void {
        if (this.state.revokeURL) {
            // revoke the object url of a changed map
            URL.revokeObjectURL(this.state.revokeURL);
        }
    }

    public render(): React.ReactNode {
        if (this.state.map) {
            return this.renderWithMap(this.state.map);
        }

        switch (this.props.map.state) {
            case CacheItemState.CACHE_UNLOADED:
                return <div>The map cache is unloaded, abort!</div>;
            case CacheItemState.CACHE_LOADING:
                return <div>Loading map...</div>;
            case CacheItemState.CACHE_ERROR:
                return <div>Error loading maps: {this.props.map.error.errorMsg}</div>;
            case CacheItemState.NOT_PRESENT:
                return <div>No maps exists with the ID, perhaps it was deleted</div>;
            case CacheItemState.PRESENT: {
                return <div>Awaiting map to be copied into state</div>;
            }
        }
    }

    private renderWithMap = (map: ConferenceMap) => {

        // list for the table
        let markerList = null;

        // marker elements to go on the map
        let markersOnMap = null;

        if (Container.isReady(this.props.markers)) {
            const markers = IDMap.values(this.props.markers.data)
                .filter(Container.isReady)
                .map(c => c.data)
                .sort(MapMarker.sortByName);

            markerList = markers.map(m => <MarkerListItem key={m.id} marker={m} updateMarker={this.props.updateMarker}
                                                          deleteMarker={this.props.deleteMarker} panTo={this.panTo}
                                                          saveMarkers={this.props.saveMarkers}/>);
            markersOnMap = markers.map(m => markerOnMap(m, this.markerDragged));
        }

        const bounds = this.state.bounds || [[0, 0], [1000, 1000]];

        const isNew = this.props.match.params.id === "new";

        let mapUrl = map.path;
        if (mapUrl.charAt(0) === "/") {
            mapUrl = process.env.PUBLIC_URL + mapUrl;
        }

        const leafletMap = <ReactLeaflet.Map crs={L.CRS.Simple} minZoom={-1} maxZoom={3} bounds={bounds}
                                             ref={this.mapRef}>
            <ReactLeaflet.ImageOverlay url={mapUrl} bounds={bounds} onLoad={this.mapImageLoad}/>
            {markersOnMap}
        </ReactLeaflet.Map>;

        let markerListSection;
        if (isNew) {
            markerListSection = <div>Please save the map to add markers.</div>;
        } else {
            markerListSection =
                <>
                    <div>
                        <button type="button" className="btn btn-primary" onClick={this.newMarker}>New Marker</button>
                    </div>
                    <br/>
                    Markers are saved automatically.
                    <table className="table table-bordered">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Desc</th>
                            <th/>
                        </tr>
                        </thead>
                        <tbody>
                        {markerList}
                        </tbody>
                    </table>
                </>;
        }


        return <>
            <h1>{isNew ? 'Creating Map' : 'Modifying Map'}</h1>
            <form>
                <div className="form-group">
                    <label htmlFor="name">Map Name</label>
                    <input type="text" className="form-control" id="name"
                           placeholder="Map Name" value={map.name} onChange={this.mapTitleChanged}/>
                </div>
                <div className="form-group">
                    <label htmlFor="name">Change Map Image</label>
                    <input type="file" className="form-control" id="image"
                           onChange={this.mapImageChanged}/>
                </div>
                <div>{this.state.statusMessage}</div>
                <div>
                    <button type="button" className="btn btn-success mr-1" onClick={this.updateToServer}>
                        Save
                    </button>
                    <button type="button" className="btn btn-outline-info" onClick={this.backToList}>Cancel</button>
                    {!isNew &&
                    <button type="button" className="btn btn-danger float-right" onClick={this.delete}>Delete</button>}
                </div>
                <br/>
                {leafletMap}
                <br/>
                {markerListSection}
            </form>
        </>;
    };

    private mapImageLoad = (e: any) => {
        const image: HTMLImageElement = e.sourceTarget.getElement();

        // We recompute the bounds to allow maps with different aspect ratios
        const aspect = image.naturalWidth / image.naturalHeight;
        let newBounds: LatLngBoundsLiteral;
        if (aspect >= 1) {
            newBounds = [[0, 0], [1000, Math.floor(aspect * 1000)]];
        } else {
            newBounds = [[0, 0], [Math.floor(1000 / aspect), 1000]];
        }

        this.setState({
            bounds: newBounds,
        });
    };

    private newMarker = () => {
        if (!this.mapRef.current || !CacheItem.isPresent(this.props.map)) {
            // no map ref means we are trying to add a marker while loading, shouldn't happen
            return;
        }

        const map = this.props.map.item;
        const leafletMap: Map = this.mapRef.current.leafletElement;
        const pos = GridPos.fromLatLng(leafletMap.getCenter());

        const newMarker = MapMarker.create(map.id, pos);

        this.props.updateMarker(newMarker);
    };

    private panTo = (marker: MapMarker) => {
        if (!this.mapRef.current) {
            return;
        }

        const leafletMap: Map = this.mapRef.current.leafletElement;

        leafletMap.flyTo(GridPos.toLatLng(marker.pos), 1);
    };

    private onUpdate = () => {
        if (!this.state.map) {
            const idOrNew = this.props.match.params.id;
            if (idOrNew === "new") {
                this.setState({
                    map: ConferenceMap.create(),
                });
            }
            if (CacheItem.isPresent(this.props.map)) {
                // TODO id or new

                this.setState({
                    map: this.props.map.item,
                });
            }
        }
    };

    private markerDragged = (marker: MapMarker, event: any) => {
        const leafletMarker = event.target as Marker;
        const latLng = leafletMarker.getLatLng();

        const pos: GridPos = {
            x: latLng.lng,
            y: latLng.lat,
        };

        this.props.updateMarker({
            ...marker,
            pos,
        });

        // save markers back to server
        this.props.saveMarkers();
    };


    private mapTitleChanged = (e: ChangeEvent<HTMLInputElement>) => {
        if (!this.state.map) {
            return;
        }

        const newTitle = e.target.value;

        this.setState(state => {
            if (!state.map) {
                throw new Error("We can't change state if we haven't loaded");
            }

            return {
                map: {
                    ...state.map,
                    name: newTitle,
                },
            };
        });
    };

    private mapImageChanged = (e: ChangeEvent<HTMLInputElement>) => {
        if (!this.state.map || !e.target.files || e.target.files.length === 0) {
            return;
        }

        const url = URL.createObjectURL(e.target.files[0]);

        this.setState(state => {
            if (!state.map) {
                throw new Error("No more state");
            }

            if (state.revokeURL) {
                // ensure old object url is revoked before we update the revoke url
                URL.revokeObjectURL(state.revokeURL);
            }

            return {
                map: {
                    ...state.map,
                    path: url,
                },
                revokeURL: url,
            }
        });
    };

    private updateToServer = () => {
        // no saving if we haven't loaded yet!
        if (!this.state.map) {
            return;
        }

        // validate
        const validationMessage = ConferenceMap.validationMessage(this.state.map);
        if (validationMessage !== null) {
            this.setState({
                statusMessage: validationMessage,
            });
            return;
        }

        // ensure markers are saved
        this.props.saveMarkers();

        const isNew = this.props.match.params.id === "new";

        this.setState({
            statusMessage: "Saving...",
        });

        this.props.updateMap(this.state.map, this.state.revokeURL)
            .then(newMap => {
                this.setState(state => {
                    if (state.revokeURL) {
                        URL.revokeObjectURL(state.revokeURL);
                    }

                    return {
                        map: newMap,
                        statusMessage: "Saved successfully!",
                        revokeURL: undefined
                    };
                });

                if (isNew) {
                    // Go to the proper page for the map
                    this.props.history.replace(`/map/${newMap.id}`);
                }
            })
            .catch(error => {
                this.setState({
                    statusMessage: `Failed to save map: ${error}`
                });
            })
    };

    private backToList = () => {
        // go back
        this.props.history.replace("/maps");
    };

    private delete = () => {
        if (!this.state.map) {
            return;
        }

        this.setState({
            statusMessage: "Deleting...",
        });

        this.props.deleteMap(this.state.map)
            .then(() => {
                this.props.history.replace("/maps");
            })
            .catch(reason => {
                this.setState({
                    statusMessage: "Failed to delete"
                });
            });
    };

}

function mapStateToProps(state: AppState, ownProps: ConnectedProps): ReduxStateProps {
    const mapId = ownProps.match.params.id;

    return {
        map: Cache.getItem(state.mapCache, mapId),
        markers: MutableCache.filter(state.markerCache, item => item.mapId === mapId),
    };
}

function mapDispatchToProps(dispatch: AppDispatch): ReduxDispatchProps {
    return {
        loadMaps: () => dispatch(loadMaps()),
        loadMarkers: () => dispatch(loadMarkers()),
        updateMap: (map, imageUrl) => updateMap(map, imageUrl, dispatch),
        deleteMap: (map) => deleteMap(map, dispatch),
        updateMarker: marker => dispatch(updateCachedMarkers(
            {
                [marker.id]: Container.modified(marker, Date.now())
            })),
        deleteMarker: marker => dispatch(updateCachedMarkers(
            {
                [marker.id]: Container.deleted(Date.now())
            })),
        saveMarkers: () => dispatch(saveMarkers()),
    }
}

export const MapPage = connect(mapStateToProps, mapDispatchToProps)(UnconnectedMapPage);

interface MarkerListItemProps {
    marker: MapMarker,
    updateMarker: (marker: MapMarker) => void,
    deleteMarker: (marker: MapMarker) => void,
    panTo: (marker: MapMarker) => void,
    saveMarkers: () => void,
}

class MarkerListItem extends React.Component<MarkerListItemProps, {}> {

    public render(): React.ReactNode {
        return <tr>
            <td className='table-half-expand'><input type='text' className='marker-table-input'
                                                     value={this.props.marker.name} name='name'
                                                     onChange={this.textChanged}
                                                     autoFocus={this.props.marker.name === "New Marker"}/></td>
            <td className='table-expand'><input type='text' className='marker-table-input'
                                                value={this.props.marker.description}
                                                name='description'
                                                onChange={this.textChanged}/></td>
            <td className='table-shrink'>
                <button type='button' className='btn btn-info mr-1' onClick={this.panTo}
                        onBlur={this.props.saveMarkers}>Locate
                </button>
                <button type='button' className='btn btn-danger' onClick={this.delete}
                        onBlur={this.props.saveMarkers}>&times;</button>
            </td>
        </tr>;
    }

    private textChanged = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        this.props.updateMarker({
            ...this.props.marker,
            [e.target.name]: e.target.value,
        });
    };

    private panTo = () => {
        this.props.panTo(this.props.marker);
    };

    private delete = () => {
        this.props.deleteMarker(this.props.marker);
    }
}

function markerOnMap(marker: MapMarker, dragHandler: (marker: MapMarker, event: any) => void) {
    // northing = y, easting = x,
    const pos = [marker.pos.y, marker.pos.x];

    const onDrag = (dragEvent: any) => {
        dragHandler(marker, dragEvent);
    };

    return <ReactLeaflet.Marker position={pos} key={marker.id} draggable={true} onDragend={onDrag}>
        <ReactLeaflet.Popup>
            <h5>{marker.name}</h5>
            {marker.description}
        </ReactLeaflet.Popup>
    </ReactLeaflet.Marker>;
}