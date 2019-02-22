using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SwipeControl : MonoBehaviour
{
	public Swipe swipe;
	public Transform box;

	private Vector3 moveTo;

	void Start()
	{
		moveTo = box.position;
	}

	void Update()
	{
		moveTo = Vector3.zero;

		// Based on swipe, manipulate transform vector
		if (swipe.GetUp())
		{
			moveTo += new Vector3 (0, 0, 20);
		}
		if (swipe.GetDown())
		{
			moveTo += new Vector3 (0, 0, -20);
		}
		if (swipe.GetLeft())
		{
			moveTo += new Vector3 (20, 0, 0);
		}
		if (swipe.GetRight())
		{
			moveTo += new Vector3 (-20, 0, 0);
		}

		// Move box towards the new transform vector in 1 second (1 * fps)
		box.position = Vector3.MoveTowards(box.position, box.position + 5*moveTo, 20*Time.deltaTime);
	}
}
