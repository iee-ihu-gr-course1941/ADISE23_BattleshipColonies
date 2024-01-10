const ENDPOINTS = {
    JOIN: '/api/game/join',
    GAME: '/api/game',
    START: '/api/game/start',
    PLACE: '/api/game/place',
    ATTACK: '/api/game/attack',
    RANDOM_SHIPS: '/api/game/randomShipPositions',
}
let PLAYER_TOKEN = null;

var groupBy = function(xs, key) {
    return xs.reduce(function(rv, x) {
        (rv[x[key]] = rv[x[key]] || []).push(x);
        return rv;
    }, {});
};

const SHIP_TYPES = {
    CARRIER: {
        size: 5,
        image: "/images/ship.png",
        cell_images: [
            "/images/ship_start.png",
            "/images/ship_middle.png",
            "/images/ship_middle.png",
            "/images/ship_middle.png",
            "/images/ship_end.png"
        ]
    },
    BATTLESHIP: {
        size: 4,
        image: "/images/ship.png",
        cell_images: [
            "/images/ship_start.png",
            "/images/ship_middle.png",
            "/images/ship_middle.png",
            "/images/ship_end.png"
        ]
    },
    PATROL_BOAT: {
        size: 3,
        image: "/images/ship.png",
        cell_images: [
            "/images/ship_start.png",
            "/images/ship_middle.png",
            "/images/ship_end.png"
        ]
    },
    SUBMARINE: {
        size: 3,
        image: "/images/ship.png",
        cell_images: [
            "/images/ship_start.png",
            "/images/ship_middle.png",
            "/images/ship_end.png"
        ]
    },
    DESTROYER: {
        size: 2,
        image: "/images/ship.png",
        cell_images: [
            "/images/ship_start.png",
            "/images/ship_end.png"
        ]
    }
}

function getRandomShipPositions(callback) {
    fetch(ENDPOINTS.RANDOM_SHIPS, {
        method: 'GET'
    }).then(response =>
        response.json().then(data => ({
                data: data,
                status: response.status
            })
        ).then(res => {
            handleError(res.data.errorMessage);
            callback(res.data);
        })
    );
}

function getGame(callback) {
    fetch(ENDPOINTS.GAME, {
        method: 'GET',
        headers: { 'X-Player-Token': PLAYER_TOKEN }
    }).then(response =>
        response.json().then(data => ({
                data: data,
                status: response.status
            })
        ).then(res => {
            handleError(res.data.errorMessage);
            callback(res.data);
        }));
}

let kill = false;
let CURRENT_GAME_SCENE_RENDERED_STATE;
let CURRENT_GAME_VERSION = 0;
let GAME_SCENES= {
    NO_GAME_JOINED: {
        matches: (game = null) => !PLAYER_TOKEN,
        renderUI: (game = null) => {
            console.log('handing no game joined');
            document.getElementById('app').innerHTML = `
                <div id="backgroundPage">
                    <div id="loginContainer">
                        <div class="loginItem">
                            <h1 id="title">The Battleship Experience</h1>
                        </div>
                        <div class="loginItem">
                            <input id="join-game-name_input" placeholder="Enter your name" />
                        </div>
                        <div class="loginItem">
                            <button id="join-game-button">Join</button>
                            <a href="scoreboard.html" id="scoreboardbutton">Scoreboards </a>
                        </div>
                    </div>
                </div>
            `;

            document.getElementById('join-game-button').addEventListener('click', async () => {
                const response = await fetch(ENDPOINTS.JOIN, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({playerName: document.getElementById('join-game-name_input').value})
                });
                PLAYER_TOKEN = response.headers.get('X-Player-Token');
                const data = await response.json();
                handleError(data.errorMessage);
                stateMachineLoop(data);
            });
        }
    },
    LOAD_GAME: {
        matches: (game = null) => PLAYER_TOKEN && (!game || game.status !== 'FINISHED'),
        updateShipSelfBoard: (boardCellList) => {
            [...document.querySelectorAll(`[data-boardcell-self-index]`)].forEach(boardCellElement => {
                boardCellElement.dataset.shipType = 'NONE';
                boardCellElement.innerHTML = '';
            })

            const shipOrientations = Object.entries(groupBy(boardCellList, 'shipType'))
                .reduce((acc, [key, shipTypeIndexes]) => {
                    const firstElement = shipTypeIndexes[0].boardIndex; // Accessing the first element
                    const lastElement = shipTypeIndexes[shipTypeIndexes.length - 1].boardIndex; // Accessing the last element

                    acc[key] = firstElement % 8 === lastElement % 8 ? 'VERTICAL' : 'HORIZONTAL';
                    return acc;
                }, {});

            let shipImageOrder = {};
            boardCellList.forEach(boardCell => {
                const boardCellElement = document.querySelector(`[data-boardcell-self-index="${boardCell.boardIndex}"]`);
                boardCellElement.dataset.shipType = boardCell.shipType;
                if (!shipImageOrder[boardCell.shipType]) {
                    shipImageOrder[boardCell.shipType] = 0;
                }

                boardCellElement.innerHTML = `<img ${shipOrientations[boardCell.shipType] === 'VERTICAL' ? 'style="transform: rotate(90deg);"' : ""} src="${SHIP_TYPES[boardCell.shipType].cell_images[shipImageOrder[boardCell.shipType]]}"  alt="shipType"/>`;
                shipImageOrder[boardCell.shipType]++;
            });
        },
        updateShipEnemyBoard: (boardCellList, shouldAttack) => {
            boardCellList.forEach(boardCell => {
                const boardCellElement = document.querySelector(`[data-boardcell-enemy-index="${boardCell.boardIndex}"]`);
                boardCellElement.dataset.attackStatus = boardCell.attackStatus;


                if (boardCell.hit) {
                    // if we have attacked this cell
                    boardCellElement.innerHTML = `HIT`;
                    if (boardCell.shipType !== 'NONE') {
                        // hit a ship
                        boardCellElement.style.backgroundColor = 'red';
                    } else {
                        // hit water
                        boardCellElement.style.backgroundColor = 'blue';
                    }
                } else if (shouldAttack) {
                    // if we can attack this cell
                    boardCellElement.onclick = () => {GAME_SCENES.LOAD_GAME.BINDINGS['clicked-board-cell'](boardCell.boardIndex)};
                    boardCellElement.style.backgroundColor = 'orange';
                } else {
                    // can't attack this cell
                    boardCellElement.onclick = () => {};
                    boardCellElement.style.backgroundColor = 'grey';
                }
            });
        }, BINDINGS: {
            "game-phase-display": (buildingPhase) =>
                document.getElementById('game-phase-display').innerHTML = buildingPhase,
            "game-board-self": (board) => {
                document.getElementById('game-board-self').innerHTML = `
                        ${Array(8).fill(0).map((_, i) => `
                            <tr>
                                ${Array(8).fill(0).map((_, j) => `
                                    <td>
                                        <button data-boardcell-self-index="${i * 8 + j}" data-ship-type="NONE"></button>
                                    </td>
                                `).join('')}
                            </tr>
                        `).join('')}
                    `
            },
            "game-board-opponent": (board) =>
                document.getElementById('game-board-opponent').innerHTML = `
                        ${Array(8).fill(0).map((_, i) => `
                            <tr>
                                ${Array(8).fill(0).map((_, j) => `
                                    <td>
                                        <button data-boardcell-enemy-index="${i * 8 + j}"></button>
                                    </td>
                                `).join('')}
                            </tr>
                        `).join('')}
                    `,
            "game-board-render-ships-and-attacks": (selfBoard, enemyBoard, shouldAttack) => {
                GAME_SCENES.LOAD_GAME.updateShipSelfBoard(selfBoard.filter(boardCell => boardCell.shipType !== 'NONE'));
                GAME_SCENES.LOAD_GAME.updateShipEnemyBoard(enemyBoard, shouldAttack);
            },
            "clicked-board-cell": (boardCellIndex) => {
                console.log('attacked board cell', boardCellIndex);
                fetch(ENDPOINTS.ATTACK, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Player-Token': PLAYER_TOKEN
                    },
                    body: JSON.stringify({"attackPosition": boardCellIndex})
                }).then(response =>
                    response.json()
                        .then(data => ({
                            data: data,
                            status: response.status
                        }))
                        .then(res => {
                            handleError(res.data.errorMessage);
                            stateMachineLoop(res.data);
                        }));
            },
            "game-actions-list": (game) => {
                console.log(game.status, ' game status');
                const actionsList = document.getElementById('game-actions-list');
                switch (game.status) {
                    case "WAITING_FOR_PLAYER_TO_JOIN" || "WAITING_FOR_PLAYER_TO_PLACE_SHIPS":
                        actionsList.innerHTML = '<span>WAITING</span>';
                        break;
                    case "CAN_START_GAME":
                        const startButton = document.createElement('button');
                        startButton.setAttribute('id', 'startButton');
                        startButton.innerHTML = 'START';
                        startButton.addEventListener('click', async () => {
                            const response = await fetch(ENDPOINTS.START, {
                                method: 'PUT',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'X-Player-Token': PLAYER_TOKEN
                                },
                                body: JSON.stringify({})
                            });
                            const data = await response.json();
                            handleError(data.errorMessage);
                            stateMachineLoop(data);
                        });
                        actionsList.appendChild(startButton);
                        break;
                    case "BUILDING":
                        getRandomShipPositions(data => GAME_SCENES.LOAD_GAME.updateShipSelfBoard(data));
                        const resetShips = document.createElement('button');
                        resetShips.setAttribute('id', 'resetShipsButton');
                        resetShips.innerHTML = 'RESET SHIPS';
                        resetShips.addEventListener('click', () => getRandomShipPositions(data => GAME_SCENES.LOAD_GAME.updateShipSelfBoard(data)));

                        const placeShips = document.createElement('button');
                        placeShips.setAttribute('id', 'placeShipsButton');
                        placeShips.innerHTML = 'CONFIRM SHIP PLACEMENTS';
                        placeShips.addEventListener('click', () => {
                            const shipPositionsMap = [...document.querySelectorAll(`[data-boardcell-self-index][data-ship-type]:not([data-ship-type='NONE'])`)]
                                .reduce((acc, boardCellElement) => {
                                    const shipType = boardCellElement.dataset.shipType;
                                    const boardIndex = boardCellElement.dataset.boardcellSelfIndex;

                                    acc[shipType] = acc[shipType] || [];
                                    acc[shipType].push(boardIndex);

                                    return acc;
                                }, {});

                            console.log("Placing ship Positions at ", shipPositionsMap);
                            fetch(ENDPOINTS.PLACE, {
                                method: 'PUT',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'X-Player-Token': PLAYER_TOKEN
                                },
                                body: JSON.stringify({"shipPlacements": shipPositionsMap})
                            }).then(response =>
                                response.json()
                                    .then(data => ({
                                        data: data,
                                        status: response.status
                                    }))
                                    .then(res => {
                                        handleError(res.data.errorMessage);
                                        stateMachineLoop(res.data);
                                    })
                            );
                        });
                        actionsList.appendChild(resetShips);
                        actionsList.appendChild(placeShips);
                        break;
                }
            }
        },
        renderUI: (game = null) => {
            document.getElementById('app').innerHTML = `
                <div id="battleBody">
                    <header>
                        <h1 id="game-phase-display"></h1>
                    </header>
                    <div id="game-area">
                        <div class="game-section">
                            <h2>Our Board</h2>
                            <table id="game-board-self" class="game-board"></table>
                        </div>
                        <div class="game-section">
                            <h2>Enemy Board</h2>
                            <table id="game-board-opponent" class="game-board"></table>
                        </div>
                    </div>
                    <footer>
                        <div id="game-actions-list"></div>
                    </footer>
                </div>
            `;
        },
        updateInterval: null
        ,
        updateUI: (game = null) => {
            console.log('handing load game', game);
            GAME_SCENES.LOAD_GAME.BINDINGS['game-phase-display'](game.status);
            GAME_SCENES.LOAD_GAME.BINDINGS['game-board-self'](game.board);
            GAME_SCENES.LOAD_GAME.BINDINGS['game-board-opponent'](game.opponentBoard);
            GAME_SCENES.LOAD_GAME.BINDINGS['game-board-render-ships-and-attacks'](game.board, game.opponentBoard, game.status === 'ATTACKING');
            GAME_SCENES.LOAD_GAME.BINDINGS['game-actions-list'](game);

            GAME_SCENES.LOAD_GAME.updateInterval = setInterval(() => {
                if (kill) {
                    console.log('Killing game update interval');
                    clearInterval(GAME_SCENES.LOAD_GAME.updateInterval);
                    return;
                }

                console.log('Checking for game updates');
                getGame(data => {
                    handleError(data.errorMessage);
                    if (parseInt(data.version, 10) > parseInt(CURRENT_GAME_VERSION, 10)) {
                        console.log('Found game updates');
                        stateMachineLoop(data);
                    } else {
                        console.log('No game updates');
                    }
                });
            }, 2000);
            CURRENT_GAME_VERSION = game.version;
        }
    },
    FINISHED: {
        matches: (game = null) => game && game.status === 'FINISHED',
        renderUI: (game) => {
            document.getElementById('app').innerHTML = `
                <h1>Game Over</h1>
                <h2 id="game-over-message"></h2>
                <h2>Clearing game after 10 seconds</h2>
            `;

            document.getElementById('game-over-message').innerHTML = game.hasWon ? 'You Won!' : 'You Lost :(';
            console.log('Clearing game');
            CURRENT_GAME_SCENE_RENDERED_STATE = null;
            GAME_SCENES.LOAD_GAME.updateInterval = null;
            CURRENT_GAME_VERSION = 0;
            PLAYER_TOKEN = null;
            kill = true;

            setTimeout(() => {
                console.log('Refreshing game');
                stateMachineLoop();
            }, 10000);
            clearInterval(GAME_SCENES.LOAD_GAME.updateInterval);
        }
    }
}

function stateMachineLoop(gameUpdate = null) {
    console.log(new Date().getTime() + '--- stateMachineLoop ---');
    for (const [key, value] of Object.entries(GAME_SCENES)) {
        if (value.matches(gameUpdate)) {
            console.log('state handled by ' + key);

            if (value.renderUI) {
                if (value !== CURRENT_GAME_SCENE_RENDERED_STATE) {
                    console.log('state handling rendering ' + key);
                    value.renderUI(gameUpdate);
                } else {
                    console.log('state already rendered ' + key);
                }
                CURRENT_GAME_SCENE_RENDERED_STATE = this;
            }

            if (value.updateUI && gameUpdate) {
                console.log('state handling update ' + key);
                value.updateUI(gameUpdate);
            }

            break;
        }
    }
}
function handleError(errorMessage) {
    if(errorMessage) {
        alert(errorMessage);
        kill = true;
        setTimeout(() => {
            location.reload()
        });
    }
}
window.onload = () => stateMachineLoop()