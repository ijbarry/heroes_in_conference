import * as React from 'react';
import {Route} from "react-router";
import './App.css';

import {EventListPage} from "./events/EventListPage";
import {MapPage} from "./map/MapPage";
import {NavLink} from "react-router-dom";
import {EventPage} from "./events/EventPage";

class App extends React.Component {
    public render() {
        return <>
            <nav className="navbar navbar-expand-lg navbar-light bg-light">
                <a className="navbar-brand" href="#">Navbar</a>
                <button className="navbar-toggler" type="button" data-toggle="collapse"
                        data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                        aria-expanded="false" aria-label="Toggle navigation">
                    <span className="navbar-toggler-icon"/>
                </button>

                <div className="collapse navbar-collapse" id="navbarSupportedContent">
                    <ul className="navbar-nav mr-auto">
                        <li className="nav-item">
                            <NavLink to="/" exact={true} className="nav-link" activeClassName="active">Home</NavLink>
                        </li>
                        <li className="nav-item">
                            <NavLink to="/map" className="nav-link" activeClassName="active">Map</NavLink>
                        </li>
                        <li className="nav-item">
                            <NavLink to="/events" className="nav-link" activeClassName="active">Events</NavLink>
                        </li>
                    </ul>
                </div>
            </nav>
            <div className="container">
                <Route path="/map" component={MapPage}/>
                <Route path="/events" component={EventListPage}/>
                <Route path="/event/:id" component={EventPage}/>
            </div>
        </>;
    }
}

export default App;
