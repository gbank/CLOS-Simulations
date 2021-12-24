""" author: thomas.fenz@univie.ac.at """

import numpy as np
import scipy.stats as st


def set_seed(seed: float = 0):
    """ sets random seed """
    np.random.seed(seed)


def get_traffic_matrix(n, scale: float = 100, fixed_total: float = None) -> np.ndarray:
    """
    Creates a traffic matrix of size n x n using the gravity model with independent exponential distributed weight vectors
    :param n: size of network (# communication nodes)
    :param scale: used for generating the exponential weight vectors (scale := 1/lambda)
    :param fixed_total: if not None, the sum of all demands are scaled to this value
        Note: scale parameter has no effect if fixed_total is set!
    :return: n x n traffic matrix as numpy.ndarray
    """
    t_in = np.array([st.expon.rvs(size=n, scale=scale)])
    t_out = np.array([st.expon.rvs(size=n, scale=scale)])

    t = (np.sum(t_in) + np.sum(t_out)) / 2  # assumption that sum(t_in) == sum(t_out) == t

    # probability matrix
    p_in = t_in / np.sum(t_in)
    p_out = t_out / np.sum(t_out)
    p_matrix = np.matmul(p_in.T, p_out)

    # traffic matrix
    t_matrix = p_matrix * t

    if fixed_total:
        multiplier = fixed_total / np.sum(t_matrix)
        t_matrix *= multiplier

    return t_matrix
